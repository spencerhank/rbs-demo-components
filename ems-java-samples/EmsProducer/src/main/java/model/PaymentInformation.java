
package model;

public class PaymentInformation {


    private String paymentMethod;

    private String cardNumber;

    private Integer cvc;

    private Address address;

    private double subTotal;

    private double tax;

    private String currency;


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Integer getCvc() {
        return cvc;
    }

    public void setCvc(Integer cvc) {
        this.cvc = cvc;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "PaymentInformation{" +
                "paymentMethod='" + paymentMethod + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", cvc=" + cvc +
                ", subTotal=" + subTotal +
                ", tax=" + tax +
                ", currency='" + currency + '\'' +
                '}';
    }
}
