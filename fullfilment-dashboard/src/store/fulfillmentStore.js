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


    return {
        currentUser,
        currentStoreValue,
        currentStoreId,
        availableFulfillmentOrders,
        handleAvailableOrdersResponse,
        disconnectSolace
    }
})