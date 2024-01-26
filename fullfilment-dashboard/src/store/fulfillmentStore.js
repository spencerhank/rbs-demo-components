import { ref, watch } from 'vue';
import { defineStore } from 'pinia'
import { useSolaceStore } from './solace';

export const useFulfillmentStore = defineStore('fulfillmentStore', () => {
    const solaceStore = useSolaceStore();

    const currentUser = ref('');
    const currentStoreValue = ref('');
    const currentStoreId = ref('');
    const availableFulfillmentOrders = ref([])

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
        solaceStore.sendRequest(payload, `request/fulfillment/orders/${currentStoreValue.value}/${currentStoreId.value}`, handleAvailableOrdersResponse)

        // Session no local must be set to true
        solaceStore.addSubscriptionHandler(`fulfillment/task/*/${currentStoreId.value}/>`, handlefilfillmentTaskUpdates);
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
                    order.rewardsInfo = updateToProcess.rewardsInfo;
                    order.storeid = updateToProcess.storeId;
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
                newOrder.rewardsInfo = updateToProcess.rewardsInfo;
                newOrder.storeid = updateToProcess.storeId;
                newOrder.storeName = updateToProcess.storeName;
                availableFulfillmentOrders.value.push(newOrder);
            }
        }

        // if (updateToProcess.order != null) {
        //     updatedOrder = updateToProcess.order
        // } else {
        //     updatedOrder = updateToProcess
        // }
        // console.log(updatedOrder)
        // if (availableFulfillmentOrders.value.length == 0) {
        //     availableFulfillmentOrders.value.push(updatedOrder);
        // } else {
        //     availableFulfillmentOrders.value.forEach(order => {
        //         var rk = order?.RowKey || order.rowKey
        //         if (rk == updatedOrder.rowKey) {
        //             order.assignedTo = updatedOrder.assignedTo;
        //             order.fulfillmentStatus = updatedOrder.fulfillmentStatus;
        //             order.products = updatedOrder.products;
        //             order.purchaseChannel = updatedOrder.purchaseChannel;
        //             order.timeStamp = updatedOrder.timeStamp;
        //             order.action = updatedOrder.action;
        //         } else {
        //             ordersToAdd.push(updatedOrder);
        //             return order;
        //         }
        //     });
        //     if (ordersToAdd.length > 0) {
        //         availableFulfillmentOrders.value.push(ordersToAdd);
        //     }

        // }



    }

    function connectToSolace() {
        solaceStore.connect();
    }

    function disconnectSolace() {
        solaceStore.disconnect();
        availableFulfillmentOrders.value = []
    }

    function handleAvailableOrdersResponse(result) {
        console.log('handleAvailableOrdersResponse');
        availableFulfillmentOrders.value = JSON.parse(result.getSdtContainer().getValue());
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
        solaceStore.publishMessage(`fulfillment/task/assigned/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`, payload);
    }

    function releaseTask(order) {
        let payload = {
            order: order,
            action: 'RELEASED',
            storeValue: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        order.assignedTo = null;
        solaceStore.publishMessage(`fulfillment/task/released/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`, payload);
    }

    return {
        currentUser,
        currentStoreValue,
        currentStoreId,
        availableFulfillmentOrders,
        handleAvailableOrdersResponse,
        disconnectSolace,
        assignTaskToSelf,
        releaseTask
    }
})