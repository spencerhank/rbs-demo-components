package kafka;

import kafka.enums.AddressEnum;
import kafka.enums.PaymentInformationEnum;
import kafka.enums.ProductEnum;
import kafka.model.Address;
import kafka.model.Order;
import kafka.model.PaymentInformation;
import kafka.model.Product;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderGenerator {
//    TODO: persist existing orders, add ability to randomly get existing order and update or cancel

    public static Order generateRandomOrder() {
        Order order = new Order();
        Address addr = AddressEnum.getRandomAddress();
        PaymentInformation paymentInfo = PaymentInformationEnum.getRandomPaymentInformation();

        Random random = new Random();
        int randomNumber = random.nextInt(15) + 1;
        List<Product> productList = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            productList.add(ProductEnum.getRandomProduct());
        }
        BigInteger subTotal = productList.stream().map(product -> product.getQuantity().multiply(product.getUsdPrice().toBigInteger())).reduce(BigInteger.ZERO, BigInteger::add);
        double taxRate = .05;
        double tax = taxRate * subTotal.doubleValue();

        paymentInfo.setSubTotal(subTotal.doubleValue());
        paymentInfo.setTax(tax);

        if(paymentInfo.getPaymentMethod().equalsIgnoreCase("cash")) {
            order.setStoreNumber("000" + random.nextInt(100));
        }
        order.setOrderNumber("000" + random.nextInt(1000));
        order.setOrderDate(LocalDateTime.now().toString());
        order.setAddress(addr);
        order.setPaymentInformation(paymentInfo);
        order.setProducts(productList);


        return order;
    }
}
