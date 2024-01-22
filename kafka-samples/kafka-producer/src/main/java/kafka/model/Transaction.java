
package kafka.model;

import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class Transaction {

    private Address address;

    private List<Product> products;

    private PaymentInformation paymentInformation;

    private String orderDate;

    private String orderNumber;

    private String storeNumber;


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public PaymentInformation getPaymentInformation() {
        return paymentInformation;
    }

    public void setPaymentInformation(PaymentInformation paymentInformation) {
        this.paymentInformation = paymentInformation;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    @Override
    public String toString() {
        return "Order{" +
                "address=" + address +
                ", products=" + products +
                ", paymentInformation=" + paymentInformation +
                ", orderDate='" + orderDate + '\'' +
                ", orderNumber=" + orderNumber +
                ", storeNumber=" + storeNumber +
                '}';
    }


}
