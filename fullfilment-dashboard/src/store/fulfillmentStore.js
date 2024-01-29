import { ref, watch } from 'vue';
import { defineStore } from 'pinia'
import { useSolaceStore } from './solace';

export const useFulfillmentStore = defineStore('fulfillmentStore', () => {
    const solaceStore = useSolaceStore();

    const currentUser = ref('');
    const currentStoreValue = ref('');
    const currentStoreId = ref('');
    const availableFulfillmentOrders = ref([])
    const subscribedTopics = ref([]);
    const publishedTopic = ref('');

    watch(
        () => currentUser.value,
        (newUser, oldUser) => {
            if (oldUser == "" && newUser != "") {
                connectToSolace();
            } else {
                if (newUser == "") {
                    disconnectSolace();
                }
            }
        }
    )

    watch(
        () => solaceStore.readyToSend,
        (newStatus, oldStatus) => {
            if (oldStatus == false && newStatus == true) {
                console.log('Request available fulfillment orders');
                requestAvailableFulfillmentOrders();
            }
        }
    )

    function requestAvailableFulfillmentOrders() {
        // request latest fulfillment orders
        let payload = {
            storeName: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        solaceStore.sendRequest(payload, `request/pickup/orders/${currentStoreValue.value}/${currentStoreId.value}`, handleAvailableOrdersResponse)
        subscribedTopics.value.push(`pickup/orders/${currentStoreValue.value}/${currentStoreId.value}/>`)

        // Session no local must be set to true
        const subscription = `pickup/task/*/${currentStoreId.value}/>`;
        subscribedTopics.value.push(subscription)
        solaceStore.addSubscriptionHandler(subscription, handlefilfillmentTaskUpdates);
    }

    function handlefilfillmentTaskUpdates(message) {
        let updateToProcess = JSON.parse(message.getSdtContainer().getValue());

        // First Item in Available Orders
        if (availableFulfillmentOrders.value.length == 0) {
            console.log('Setting available Fulfillment ORders', availableFulfillmentOrders.value);
            availableFulfillmentOrders.value.push(updateToProcess)
        } else if (!Array.isArray(updateToProcess) && updateToProcess.order != null) {
            console.log('Update task assignment');
            availableFulfillmentOrders.value.forEach(order => {
                var rk = order?.RowKey || order.rowKey
                if (rk == updateToProcess.order.RowKey) {
                    console.log('Match order information', rk);
                    order.assignedTo = updateToProcess.order.assignedTo;
                    order.fulfillmentStatus = updateToProcess.order.fulfillmentStatus;
                    order.action = updateToProcess.action;
                }
            });
        } else {
            console.log('Update coming for backend')
            let foundMatch = false;
            availableFulfillmentOrders.value.forEach(order => {
                var rk = order?.RowKey || order.rowKey
                if (rk == updateToProcess.rowKey) {
                    console.log('Found order to update');
                    order.RowKey = updateToProcess.rowKey;
                    order.Timestamp = updateToProcess.timestamp;
                    order.ID = updateToProcess.ID;
                    order.action = updateToProcess.action;
                    order.paymentInformation = updateToProcess.paymentInformation;
                    order.products = updateToProcess.products;
                    order.purchaseChannel = updateToProcess.purchaseChannel;
                    order.pickUpTime = updateToProcess.pickUpTime;
                    order.rewardsInfo = updateToProcess.rewardsInfo;
                    order.storeId = updateToProcess.storeId;
                    order.storeName = updateToProcess.storeName;
                }
            });
            if (!foundMatch) {
                console.log('New Order from the backend');
                let newOrder = {}
                newOrder.RowKey = updateToProcess.rowKey;
                newOrder.Timestamp = updateToProcess.timestamp;
                newOrder.ID = updateToProcess.ID;
                newOrder.action = updateToProcess.action;
                newOrder.paymentInformation = updateToProcess.paymentInformation;
                newOrder.products = updateToProcess.products;
                newOrder.purchaseChannel = updateToProcess.purchaseChannel;
                newOrder.pickUpTime = updateToProcess.pickUpTime;
                newOrder.rewardsInfo = updateToProcess.rewardsInfo;
                newOrder.storeId = updateToProcess.storeId;
                newOrder.storeName = updateToProcess.storeName;
                availableFulfillmentOrders.value.push(newOrder);
            }
        }

    }

    function connectToSolace() {
        solaceStore.connect();
    }

    function disconnectSolace() {
        solaceStore.disconnect();
        availableFulfillmentOrders.value = []
        subscribedTopics.value = []
        publishedTopic.value = ''
    }

    function handleAvailableOrdersResponse(result) {
        console.log('handleAvailableOrdersResponse');
        let orders = JSON.parse(result.getSdtContainer().getValue());
        orders.forEach(order => {
            if (order.action != 'COMPLETED') {
                availableFulfillmentOrders.value.push(order)
            }
        })
        console.log(availableFulfillmentOrders.value);
    }

    function assignTaskToSelf(order) {
        let payload = {
            order: order,
            action: 'ASSIGNED',
            storeValue: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        order.assignedTo = currentUser.value;
        console.log(payload)
        publishedTopic.value = `pickup/task/assigned/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`
        solaceStore.publishMessage(publishedTopic.value, payload);
    }

    function releaseTask(order) {
        let payload = {
            order: order,
            action: 'RELEASED',
            storeValue: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        order.assignedTo = null;
        publishedTopic.value = `pickup/task/released/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`
        solaceStore.publishMessage(publishedTopic.value, payload);
    }

    function completeTask(order) {
        let payload = {
            order: order,
            action: 'COMPLETED',
            storeValue: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        order.fulfillmentStatus = 'COMPLETED'
        publishedTopic.value = `pickup/task/completed/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`
        solaceStore.publishMessage(publishedTopic.value, payload);
    }

    return {
        currentUser,
        currentStoreValue,
        currentStoreId,
        availableFulfillmentOrders,
        subscribedTopics,
        publishedTopic,
        handleAvailableOrdersResponse,
        disconnectSolace,
        assignTaskToSelf,
        releaseTask,
        completeTask
    }
})