package enums;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public enum StoreEnum {
    FOOD_LION("store", "1000"),
    STOP_AND_SHOP("store", "1001"),
    GIANT("store", "1002"),
    GIANT_FOOD_CO_1("store", "1003"),
    GIANT_FOOD_CO_2("store", "1004"),
    GIANT_FOOD_CO_3("store", "1005"),
    HANNAFORD("store", "1006"),
    PEAPOD("delivery", "1007"),
    FULFILLMENT_CENTER_1("fulfillment", "1008"),
    FULFILLMENT_CENTER_2("fulfillment", "1009"),
    FULFILLMENT_CENTER_3("fulfillment", "1010");

    private final String storeType;
    private final String storeId;

    StoreEnum(String storeType, String storeId) {
        this.storeType = storeType;
        this.storeId = storeId;
    }

    public String getStoreType() {
        return storeType;
    }
    public String getStoreId() {
        return storeId;
    }

    public static List<StoreEnum> getBrickAndMortarStores() {
        return Arrays.stream(StoreEnum.values()).filter(e -> e.getStoreType().equalsIgnoreCase("store")).collect(Collectors.toList());
    }

    public static List<StoreEnum> getOnlineDeliveryStores() {
        return Arrays.stream(StoreEnum.values()).filter(e -> e.getStoreType().equalsIgnoreCase("delivery")).collect(Collectors.toList());
    }

    public static List<StoreEnum> getPickUpLocations() {
        return Arrays.stream(StoreEnum.values()).filter(e -> !e.getStoreType().equalsIgnoreCase("delivery")).collect(Collectors.toList());
    }

    public static StoreEnum getRandomBrickAndMortarStore() {
        Random random = new Random();
        return getBrickAndMortarStores().get(random.nextInt(getBrickAndMortarStores().size()));
    }

    public static StoreEnum getRandomOnlineDeliveryStores() {
        Random random = new Random();
        return getOnlineDeliveryStores().get(random.nextInt(getOnlineDeliveryStores().size()));
    }

    public static StoreEnum getRandomPickUpLocation() {
        Random random = new Random();
        return getPickUpLocations().get(random.nextInt(getPickUpLocations().size()));
    }


}
