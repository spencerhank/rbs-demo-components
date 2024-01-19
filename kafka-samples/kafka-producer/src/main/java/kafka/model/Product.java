package kafka.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Product {
    private String name;
    private BigInteger quantity;
    private BigDecimal usdPrice;
    private String productNumber;

    // Constructors (default and parameterized)

    public Product(String name, Integer quantity, double usdPrice, String productNumber) {
    }

    public Product(String name, BigInteger quantity, BigDecimal usdPrice, String productNumber) {
        this.name = name;
        this.quantity = quantity;
        this.usdPrice = usdPrice;
        this.productNumber = productNumber;
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

    public BigDecimal getUsdPrice() {
        return usdPrice;
    }

    public void setUsdPrice(BigDecimal usdPrice) {
        this.usdPrice = usdPrice;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    // toString() method for easy debugging

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", usdPrice=" + usdPrice +
                ", productNumber='" + productNumber + '\'' +
                '}';
    }
}
