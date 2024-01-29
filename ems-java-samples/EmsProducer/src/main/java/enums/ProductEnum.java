package enums;


import model.Item;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum ProductEnum {
// TODO: add more products
    PRODUCT_1("White Corn Tortilla Chips BiteSize Rounds", BigInteger.valueOf(5), BigDecimal.valueOf(2.39), "356526"),
    PRODUCT_2("Nature's Promise Ground Turkey 93% Lean 7%", BigInteger.valueOf(3), BigDecimal.valueOf(4.29), "327396"),
    PRODUCT_3("Whole Milk Vitamin D", BigInteger.valueOf(2), BigDecimal.valueOf(3.59), "296420"),
    PRODUCT_4("Creamy Peanut Butter", BigInteger.valueOf(1), BigDecimal.valueOf(4.69), "1007627"),
    PRODUCT_5("Plain English Muffins", BigInteger.valueOf(1), BigDecimal.valueOf(1.79), "931005"),
    PRODUCT_6("Beef Chuck Roast", BigInteger.valueOf(1), BigDecimal.valueOf(6.99), "311558"),
    PRODUCT_7("Nature's Promise Spring Water", BigInteger.valueOf(3), BigDecimal.valueOf(1.39), "1001921"),
    PRODUCT_8("Taste of Inspirations Made In Italy Cavatappi", BigInteger.valueOf(2), BigDecimal.valueOf(2.39), "1014986");


    private final Item item;

    ProductEnum(String name, BigInteger quantity, BigDecimal usdPrice, String productNumber) {
        this.item = new Item(name, quantity,usdPrice,productNumber);
    }

    public Item getProduct() {
        return item;
    }

    public static List<ProductEnum> getAllProducts() { return Arrays.asList(values());}

    public static Item getRandomProduct() {
        Random random = new Random();
        return values()[random.nextInt(values().length)].getProduct();
    }
}
