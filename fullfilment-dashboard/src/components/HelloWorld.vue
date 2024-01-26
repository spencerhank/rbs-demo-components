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
      <div v-if="fulfillmentStore.availableFulfillmentOrders.length > 0">
        <v-row
          class="d-flex align-center justify-center"
          v-for="(item, index) in fulfillmentStore.availableFulfillmentOrders"
          :key="item.transactionId"
        >
          <v-card width="500" elevation="10" class="mt-10">
            <v-card-item>
              <v-card-title align="left" class="ml-4">
                {{ storeNameLookUp(item.storeName) }}
              </v-card-title>
              <v-card-subtitle align="left" class="ml-4"
                >Store Id: {{ item.storeId }}<br />
                Last Updated: {{ item.Timestamp }}</v-card-subtitle
              >
              <v-card-text align="left" class="ml-3">
                <v-row
                  v-for="(product, index) in formatProducts(item.products)"
                  :key="product.productId"
                >
                  <p class="font-weight-bold">{{ product.name }}</p>
                  : {{ product.quantity }}
                </v-row>
              </v-card-text>
              <v-card-actions v-if="item.action != 'CANCELLED'">
                <v-btn
                  color="primary"
                  variant="tonal"
                  elevation="1"
                  v-if="item.assignedTo == null || item.assignedTo == ''"
                  @click.prevent="fulfillmentStore.assignTaskToSelf(item)"
                  >Assign To Self</v-btn
                >
                <v-btn
                  color="primary"
                  v-if="item.assignedTo == userName"
                  variant="outlined"
                  elevation="1"
                  @click.prevent="fulfillmentStore.releaseTask(item)"
                  >Release Task</v-btn
                >
                <v-btn
                  color="secondary"
                  disabled
                  v-if="
                    item.assignedTo != null &&
                    item.assignedTo != '' &&
                    item.assignedTo != userName
                  "
                  >Task Assigned to: {{ item.assignedTo }}</v-btn
                >
              </v-card-actions>
              <v-card-actions v-if="item.action == 'CANCELLED'">
                <v-btn disabled variant="tonal">Cancelled</v-btn>
              </v-card-actions>
            </v-card-item>
          </v-card>
        </v-row>
      </div>
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

function formatProducts(products) {
  products = JSON.parse(products);
  let formattedProducts = {};
  if (Array.isArray(products)) {
    products.forEach((product) => {
      let jsonProduct = product;
      if (product.productId == null) {
        jsonProduct = JSON.parse(product);
      }
      if (formattedProducts[jsonProduct.productId]) {
        var q = formattedProducts[jsonProduct.productId].quantity;
        q = q + jsonProduct.quantity;
        formattedProducts[jsonProduct.productId].quantity = q;
      } else {
        formattedProducts[jsonProduct.productId] = jsonProduct;
      }
    });
  } else {
    formattedProducts[products.productId] = products;
  }

  console.log(formattedProducts);
  return formattedProducts;
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
