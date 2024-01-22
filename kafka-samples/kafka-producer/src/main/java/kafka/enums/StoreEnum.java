package kafka.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public enum StoreEnum {
    STOP_AND_SHOP("store"),
    GIANT("store"),
    GIANT_FOOD_CO("store"),
    HANNAFORD("store"),
    PEAPOD("delivery"),
    FULFILLMENT_CENTER_1("fulfillment"),
    FULFILLMENT_CENTER_2("fulfillment"),
    FULFILLMENT_CENTER_3("fulfillment");

    private final String storeType;

    StoreEnum(String storeType) {
        this.storeType = storeType;
    }

    public String getStoreType() {
        return storeType;
    }

    public static List<StoreEnum> getBrickAndMortarStores() {
        return Arrays.stream(StoreEnum.values()).filter(e -> e.getStoreType().equalsIgnoreCase("store")).collect(Collectors.toList());
    }

    public static List<StoreEnum> getOnlineDeliveryStores() {
        return Arrays.stream(StoreEnum.values()).filter(e -> e.getStoreType().equalsIgnoreCase("delivery")).collect(Collectors.toList());
    }

    public static List<StoreEnum> getPickUpLocations() {
        return Arrays.stream(StoreEnum.values()).filter(e -> e.getStoreType().equalsIgnoreCase("fulfillment")).collect(Collectors.toList());
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
