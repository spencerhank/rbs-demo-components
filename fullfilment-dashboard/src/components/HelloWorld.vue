<template>
  <v-container class="fill-height">
    <v-responsive class="align-start text-center fill-height">
      <div v-if="!showGreeting" class="text-h2 font-weight-bold mb-n1">
        Welcome Please Login
      </div>
      <div class="py-6"></div>

      <v-row class="d-flex">
        <v-col cols="4" class="mt-auto" v-if="showGreeting">
          <div class="text-h5">Subscribed Topics:</div>
          <v-spacer></v-spacer>
          <div
            class="subtitle-1"
            v-for="topic in fulfillmentStore.subscribedTopics"
            :key="topic"
          >
            {{ topic }}
          </div>
        </v-col>
        <v-col cols="4" class="mx-auto">
          <v-btn
            color="primary"
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
          <v-img
            class="mx-auto"
            :width="300"
            cover
            aspect-ratio="16/9"
            :src="storeImageLookUp()"
          ></v-img>

          <h2 v-if="showGreeting" class="mx-auto">
            <div class="py-2"></div>
            Hello {{ userName }}
            <v-btn
              density="compact"
              icon="mdi-logout"
              class="ml-4"
              @click="logout()"
            ></v-btn>
          </h2>
        </v-col>
        <v-col cols="4" class="mt-auto" v-if="showGreeting">
          <div class="text-h5">Published Topic:</div>
          <v-spacer></v-spacer>
          <div class="subtitle-1">{{ fulfillmentStore.publishedTopic }}</div>
        </v-col>
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
      <v-row v-if="fulfillmentStore.availableFulfillmentOrders.length > 0">
        <!-- Update to use filtered and sorted list -->
        <v-col
          cols="4"
          class="d-flex align-center justify-center"
          v-for="(item, index) in filterAndSortAvailableOrders(
            fulfillmentStore.availableFulfillmentOrders
          )"
          :key="item.transactionId"
        >
          <v-card
            width="500"
            height="215"
            elevation="10"
            class="mt-10 card-outter overflow-scroll"
          >
            <v-card-item>
              <v-card-title align="left" class="ml-4">
                {{ storeNameLookUp(item.storeName) }}
                ({{ item.storeId }})
              </v-card-title>
              <v-card-subtitle align="left" class="ml-4">
                Pickup Time:
                {{ formattedDate(item.pickUpTime) }}</v-card-subtitle
              >
              <v-card-text align="left" class="ml-3 product-text">
                <v-row
                  v-for="(product, index) in formatProducts(item.products)"
                  :key="product.productId"
                >
                  <p class="font-weight-bold">{{ product.name }}</p>
                  : {{ product.quantity }}
                </v-row>
              </v-card-text>
              <v-card-actions
                v-if="item.action != 'CANCELLED'"
                class="card-actions"
              >
                <v-btn
                  color="primary"
                  variant="tonal"
                  elevation="1"
                  v-if="item.assignedTo == null || item.assignedTo == ''"
                  @click.prevent="fulfillmentStore.assignTaskToSelf(item)"
                  >Assign To Self</v-btn
                >
                <v-btn
                  color="secondary"
                  variant="tonal"
                  v-if="
                    item.assignedTo == userName &&
                    item.fulfillmentStatus != 'COMPLETED'
                  "
                  elevation="1"
                  @click.prevent="fulfillmentStore.completeTask(item)"
                  >Complete Task</v-btn
                >
                <v-btn
                  color="primary"
                  v-if="
                    item.assignedTo == userName &&
                    item.fulfillmentStatus != 'COMPLETED'
                  "
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
                    item.assignedTo != userName &&
                    item.fulfillmentStatus != 'COMPLETED'
                  "
                  >Task Assigned to: {{ item.assignedTo }}</v-btn
                >
              </v-card-actions>
              <v-card-actions
                v-if="item.action == 'CANCELLED'"
                class="card-actions"
              >
                <v-btn disabled variant="tonal">Cancelled</v-btn>
              </v-card-actions>
              <v-card-actions
                v-if="item.fulfillmentStatus == 'COMPLETED'"
                class="card-actions"
              >
                <v-btn disabled variant="tonal">Completed</v-btn>
              </v-card-actions>
            </v-card-item>
          </v-card>
        </v-col>
      </v-row>
    </v-responsive>
  </v-container>
</template>

<style scoped>
.card-outter {
  padding-bottom: 50px;
}
.card-actions {
  position: absolute;
  bottom: 10px;
}
.product-text {
  overflow-y: scroll;
  max-height: 65px;
}
</style>

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
    icon: "https://media.aholddelhaize.com/media/hvmhcpf2/foodlion-2x.svg?t=637545970709830000",
    storeIds: ["1000"],
  },
  {
    title: "Stop & Shop",
    value: "STOP_AND_SHOP",
    icon: "https://media.aholddelhaize.com/media/1i3fpg1g/stopandshop.svg?t=637546103655330000",
    storeIds: ["1001"],
  },
  {
    title: "Giant",
    value: "GIANT",
    icon: "https://media.aholddelhaize.com/media/yqwl5eab/giant-2x.svg?t=637545970723430000",
    storeIds: ["1002"],
  },
  {
    title: "Giant Co",
    value: "GIANT_FOOD_CO",
    icon: "https://media.aholddelhaize.com/media/dz5bu1w3/giantcompany-2x.svg?t=637545970727200000",
    storeIds: ["1003", "1004", "1005"],
  },
  {
    title: "Hannaford",
    value: "HANNAFORD",
    icon: "https://media.aholddelhaize.com/media/44qjep4p/hannaford.svg?t=637546102294000000",
    storeIds: ["1006"],
  },
  {
    title: "Fulfillment Center 1",
    value: "FULFILLMENT_CENTER_1",
    icon: "https://www.aholddelhaize.com/media/zh5d2t02/ahold-delhaize-4x.png",
    storeIds: ["1008"],
  },
  {
    title: "Fulfillment Center 2",
    value: "FULFILLMENT_CENTER_1",
    icon: "https://www.aholddelhaize.com/media/zh5d2t02/ahold-delhaize-4x.png",
    storeIds: ["1009"],
  },
  {
    title: "Fulfillment Center 3",
    value: "FULFILLMENT_CENTER_1",
    icon: "https://www.aholddelhaize.com/media/zh5d2t02/ahold-delhaize-4x.png",
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

function filterAndSortAvailableOrders(allAvailableOrders) {
  // TODO sort by last updated
  // TODO filter out status
  return allAvailableOrders.sort((a, b) => {
    return new Date(a.pickUpTime) - new Date(b.pickUpTime);
  });
}

function formattedDate(dateString) {
  const date = new Date(dateString);
  const quarter = Math.round(date.getMinutes() / 15) * 15;

  return `${date.toDateString()} at ${date
    .getHours()
    .toString()
    .padStart(2, "0")}:${quarter.toString().padStart(2, "0")}`;
}

function storeNameLookUp(storeValue) {
  let returnName;
  stores.value.forEach((store) => {
    if (store.value == storeValue) {
      returnName = store.title;
    }
  });
  return returnName;
}

function storeImageLookUp() {
  let returnImage;
  stores.value.forEach((store) => {
    if (store.value == fulfillmentStore.currentStoreValue) {
      returnImage = store.icon;
    }
  });
  return returnImage;
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
