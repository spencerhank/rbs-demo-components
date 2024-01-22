package enums;


import model.Item;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public enum ProductEnum {
// TODO: add more products
    PRODUCT_1("Food Lion White Corn Tortilla Chips BiteSize Rounds", BigInteger.valueOf(5), BigDecimal.valueOf(2.39), "356526"),
    PRODUCT_2("Nature's Promise Ground Turkey 93% Lean 7%", BigInteger.valueOf(3), BigDecimal.valueOf(4.29), "327396"),
    PRODUCT_3("Food Lion Whole Milk Vitamin D", BigInteger.valueOf(2), BigDecimal.valueOf(3.59), "296420"),
    PRODUCT_4("Product Name 4", BigInteger.valueOf(1), BigDecimal.valueOf(15.99), "901234"),
    PRODUCT_5("Food Lion Beef Chuck Roast", BigInteger.valueOf(4), BigDecimal.valueOf(6.99), "311558");


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
