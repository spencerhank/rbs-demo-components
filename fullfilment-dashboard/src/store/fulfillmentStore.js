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
        solaceStore.addSubscriptionHandler(`fulfillment/task/*/${currentStoreValue.value}/${currentStoreId.value}/>`, handlefilfillmentTaskUpdates);
    }

    function handlefilfillmentTaskUpdates(message) {
        let updateToProcess = JSON.parse(message.getSdtContainer().getValue());
        availableFulfillmentOrders.value.forEach(order => {
            if (order.RowKey == updateToProcess.order.RowKey) {
                order.assignedTo = updateToProcess.order.assignedTo;
                order.fulfillmentStatus = updateToProcess.order.fulfillmentStatus;
            } else {
                return order;
            }
        });
        // console.log(updateToProcess);

    }

    function connectToSolace() {
        solaceStore.connect();
    }

    function disconnectSolace() {
        solaceStore.disconnect();
        availableFulfillmentOrders.value = []
    }

    function handleAvailableOrdersResponse(result) {
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
        solaceStore.publishMessage(`fulfillment/task/assigned/${currentStoreValue.value}/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`, payload);
    }

    function releaseTask(order) {
        let payload = {
            order: order,
            action: 'RELEASED',
            storeValue: currentStoreValue.value,
            storeId: currentStoreId.value
        }
        order.assignedTo = null;
        solaceStore.publishMessage(`fulfillment/task/released/${currentStoreValue.value}/${currentStoreId.value}/${order.RowKey}/${currentUser.value}`, payload);
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