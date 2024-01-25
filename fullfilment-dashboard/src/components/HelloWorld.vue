<template>
  <v-container class="fill-height">
    <v-responsive class="align-start text-center fill-height">
      <div class="text-body-2 font-weight-light mb-n1">Welcome to the</div>

      <h1 class="text-h2 font-weight-bold">Order Fulfillment Dashboard</h1>

      <div class="py-6" />

      <v-row class="d-flex align-center justify-center">
        <v-col cols="auto">
          <v-btn
            color="primary"
            href="https://vuetifyjs.com/introduction/why-vuetify/#feature-guides"
            min-width="228"
            rel="noopener noreferrer"
            size="x-large"
            target="_blank"
            variant="flat"
            @click.prevent="showLoginForm = !showLoginForm"
            v-if="!showLoginForm && !showGreeting"
          >
            <v-icon icon="mdi-login" size="large" start />

            Log In
          </v-btn>

          <h2 v-if="showGreeting">
            Hello {{ userName }}
            <v-btn
              density="compact"
              icon="mdi-logout"
              class="ml-4"
              @click="logout()"
            ></v-btn>
          </h2>
        </v-col>

        <!-- <v-col cols="auto">
          <v-btn
            href="https://community.vuetifyjs.com/"
            min-width="164"
            rel="noopener noreferrer"
            target="_blank"
            variant="text"
          >
            <v-icon icon="mdi-account-group" size="large" start />

            Community
          </v-btn>
        </v-col> -->
      </v-row>
      <div class="py-6"></div>
      <v-row class="d-flex align-center justify-center" v-if="showLoginForm">
        <v-col cols="auto w-25">
          <v-form @submit.prevent>
            <v-text-field v-model="userName" label="Name"> </v-text-field>
            <v-select v-model="selectedStore" label="Store" :items="stores">
            </v-select>
            <v-select
              v-if="selectedStoreIds.length > 0"
              v-model="selectedStoreId"
              label="Store Id"
              :items="selectedStoreIds"
            >
            </v-select>
            <v-btn
              color="secondary"
              block
              v-if="canLogIn()"
              @click="logUserIn()"
              >Submit</v-btn
            >
          </v-form>
        </v-col>
      </v-row>
      <v-row
        v-if="
          fulfillmentStore.availableFulfillmentOrders.length == 0 &&
          selectedStoreId != '' &&
          showLoginForm != true
        "
        class="d-flex align-center justify-center"
      >
        <v-progress-circular
          indeterminate
          color="primary"
        ></v-progress-circular>
      </v-row>
      <v-row
        class="d-flex align-center justify-center"
        v-if="fulfillmentStore.availableFulfillmentOrders.length > 0"
      >
        <v-card
          width="500"
          elevation="10"
          v-for="(item, index) in fulfillmentStore.availableFulfillmentOrders"
          :key="item.transactionId"
        >
          <v-card-item>
            <v-card-title align="left" class="ml-4">
              {{ storeNameLookUp(item.storeName) }}
            </v-card-title>
            <v-card-subtitle align="left" class="ml-4"
              >Store Id: {{ item.storeId }}</v-card-subtitle
            >
            <v-card-text align="left">{{
              // TODO: update to display all order items
              item.paymentInformation
            }}</v-card-text>
            <v-card-actions>
              <v-btn color="primary">Assign To Self</v-btn>
            </v-card-actions>
          </v-card-item>
        </v-card>
      </v-row>
    </v-responsive>
  </v-container>
</template>

<script setup>
import { ref, watch } from "vue";
import { useFulfillmentStore } from "../store/fulfillmentStore";

const fulfillmentStore = useFulfillmentStore();

const showLoginForm = ref(false);
const showGreeting = ref(false);
const userName = ref("");
const selectedStore = ref("");
const selectedStoreId = ref("");
const selectedStoreIds = ref([]);
const stores = ref([
  {
    title: "Food Lion",
    value: "FOOD_LION",
    storeIds: ["1000"],
  },
  {
    title: "Stop & Shop",
    value: "STOP_AND_SHOP",
    storeIds: ["1001"],
  },
  {
    title: "Giant",
    value: "GIANT",
    storeIds: ["1002"],
  },
  {
    title: "Giant Co",
    value: "GIANT_FOOD_CO",
    storeIds: ["1003", "1004", "1005"],
  },
  {
    title: "Hannaford",
    value: "HANNAFORD",
    storeIds: ["1006"],
  },
  {
    title: "Fulfillment Center 1",
    value: "FULFILLMENT_CENTER_1",
    storeIds: ["1008"],
  },
  {
    title: "Fulfillment Center 2",
    value: "FULFILLMENT_CENTER_1",
    storeIds: ["1009"],
  },
  {
    title: "Fulfillment Center 3",
    value: "FULFILLMENT_CENTER_1",
    storeIds: ["1010"],
  },
]);

watch(
  () => selectedStore.value,
  (selectedStore) => {
    selectedStoreIds.value = [];
    selectedStoreId.value = "";
    stores.value.forEach((store) => {
      if (store.value == selectedStore) {
        selectedStoreIds.value = store.storeIds;
      }
    });
  }
);

function storeNameLookUp(storeValue) {
  let returnName;
  stores.value.forEach((store) => {
    if (store.value == storeValue) {
      returnName = store.title;
    }
  });
  return returnName;
}

function canLogIn() {
  if (userName.value && selectedStore.value && selectedStoreId.value) {
    return true;
  } else {
    return false;
  }
}

function logUserIn() {
  // TODO: connect to solace
  fulfillmentStore.currentUser = userName.value;
  fulfillmentStore.currentStoreValue = selectedStore.value;
  fulfillmentStore.currentStoreId = selectedStoreId.value;
  showLoginForm.value = false;
  showGreeting.value = true;
}

function logout() {
  // TODO disconnect from Solace
  showLoginForm.value = false;
  showGreeting.value = false;
  selectedStore.value = "";
  selectedStoreIds.value = [];
  selectedStoreId.value = "";
  userName.value = "";
  fulfillmentStore.currentUser = "";
  fulfillmentStore.currentStoreValue = "";
  fulfillmentStore.currentStoreId = "";
  fulfillmentStore.disconnectSolace();
}
</script>
