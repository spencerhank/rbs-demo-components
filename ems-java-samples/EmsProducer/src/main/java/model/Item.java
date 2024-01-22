package model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Item {
    private String name;
    private BigInteger quantity;
    private BigDecimal price;
    private String productId;

    // Constructors (default and parameterized)

    public Item(String name, Integer quantity, double usdPrice, String productId) {
    }

    public Item(String name, BigInteger quantity, BigDecimal usdPrice, String productId) {
        this.name = name;
        this.quantity = quantity;
        this.price = usdPrice;
        this.productId = productId;
    }

    // Getter and Setter methods for each field

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigInteger getQuantity() {
        return quantity;
    }

    public void setQuantity(BigInteger quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    // toString() method for easy debugging

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", usdPrice=" + price +
                ", productId='" + productId + '\'' +
                '}';
    }
}
