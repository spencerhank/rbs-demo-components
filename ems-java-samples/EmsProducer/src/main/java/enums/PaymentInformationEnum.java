package enums;


import model.Address;
import model.PaymentInformation;

import java.util.Random;

public enum PaymentInformationEnum {
    PAYMENT_1("Credit", "1234-5678-9012-3456", 123, AddressEnum.getRandomAddress(), 100.0, 8.0, "USD"),
    PAYMENT_2("Cash", null, null, null, 50.0, 5.0, "USD"),
    PAYMENT_3("Credit", "9876-5432-1098-7654", 456, AddressEnum.getRandomAddress(), 75.0, 6.5, "USD"),
    PAYMENT_4("Cash", null, null, null, 120.0, 10.0, "USD"),
    PAYMENT_5("Credit", "5678-9012-3456-7890", 789, AddressEnum.getRandomAddress(), 90.0, 7.5, "USD"),
    PAYMENT_6("Cash", null, null, null, 60.0, 5.5, "USD"),
    PAYMENT_7("Credit", "4321-8765-0987-6543", 321, AddressEnum.getRandomAddress(), 110.0, 9.0, "USD"),
    PAYMENT_8("Cash", null, null, null, 85.0, 7.0, "USD"),
    PAYMENT_9("Credit", "8765-4321-0987-6543", 654, AddressEnum.getRandomAddress(), 70.0, 6.0, "USD"),
    PAYMENT_10("Cash", null, null, null, 95.0, 8.5, "USD"),
    PAYMENT_11("Credit", "3456-7890-1234-5678", 987, AddressEnum.getRandomAddress(), 80.0, 7.0, "USD"),
    PAYMENT_12("Cash", null, null, null, 65.0, 5.5, "USD"),
    PAYMENT_13("Credit", "7890-1234-5678-9012", 234, AddressEnum.getRandomAddress(), 115.0, 9.5, "USD"),
    PAYMENT_14("Cash", null, null, null, 55.0, 4.5, "USD"),
    PAYMENT_15("Credit", "6789-0123-4567-8901", 543, AddressEnum.getRandomAddress(), 100.0, 8.0, "USD"),
    PAYMENT_16("Cash", null, null, null, 75.0, 6.5, "USD"),
    PAYMENT_17("Credit", "8901-2345-6789-0123", 876, AddressEnum.getRandomAddress(), 120.0, 10.0, "USD"),
    PAYMENT_18("Cash", null, null, null, 90.0, 7.5, "USD"),
    PAYMENT_19("Credit", "5432-1098-7654-3210", 123, AddressEnum.getRandomAddress(), 60.0, 5.5, "USD"),
    PAYMENT_20("Cash", null, null, null, 95.0, 8.5, "USD");

    private final PaymentInformation paymentInformation;

    PaymentInformationEnum(String paymentMethod, String cardNumber, Integer cvc, Address address,
                           double subTotal, double tax, String currency) {
        this.paymentInformation = new PaymentInformation();
        this.paymentInformation.setPaymentMethod(paymentMethod);
        this.paymentInformation.setCardNumber(cardNumber);
        this.paymentInformation.setCvc(cvc);
        this.paymentInformation.setAddress(address);
        this.paymentInformation.setSubTotal(subTotal);
        this.paymentInformation.setTax(tax);
        this.paymentInformation.setCurrency(currency);
    }

    public PaymentInformation getPaymentInformation() {return paymentInformation;}

    public static PaymentInformation getRandomPaymentInformation() {
        Random random = new Random();
        return values()[random.nextInt(values().length)].getPaymentInformation();
    }
}
