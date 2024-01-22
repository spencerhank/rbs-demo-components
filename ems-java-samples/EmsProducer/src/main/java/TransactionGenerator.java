import enums.*;
import model.Item;
import model.PaymentInformation;
import model.PurchaseChannel;
import model.Transaction;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionGenerator {
    //    TODO: persist existing orders, add ability to randomly get existing order and update or cancel
    public static ConcurrentHashMap<String, Transaction> transactionMap = new ConcurrentHashMap<>();
    public static List<String> transactionKeys = new ArrayList<>();


    public static Transaction generateRandomOrder() {
        Random random = new Random();
        int probability = 10;
        Transaction transaction = new Transaction();
        String transactionIdToUpdate = null;
        int transactionKeyIndexToUpdate = -1;
        PaymentInformation paymentInformation;

        if (!transactionMap.isEmpty() && random.nextInt(probability) == 0) {
            // Update Or Cancel an existing transaction
            transactionKeyIndexToUpdate = random.nextInt(transactionKeys.size());
            Transaction transactionToModify = transactionMap.get(transactionKeys.get(transactionKeyIndexToUpdate));
            if (random.nextBoolean()) {
                // cancel transaction
                transactionToModify.setTransactionAction(TransactionActionEnum.CANCELLED);
                transactionMap.remove(transactionKeys.get(0));
                transactionKeys.remove(0);
                return transactionToModify;
            } else {
                // update transaction

                transaction = transactionMap.get(transactionKeys.get(transactionKeyIndexToUpdate));
                transactionIdToUpdate = transaction.getTransactionId();
                transaction.setTransactionAction(TransactionActionEnum.UPDATED);
                transaction.setProducts(getRandomItemsForTransaction());
                // make sure paymentInfo.address doesn't change
                paymentInformation = transaction.getPaymentInformation();
                transactionKeys.remove(0);
            }
        } else {
            transaction.setTransactionAction(TransactionActionEnum.CREATED);
            transaction.setTransactionId(UUID.randomUUID().toString());
            transaction.setProducts(getRandomItemsForTransaction());
            paymentInformation = PaymentInformationEnum.getRandomPaymentInformation();
        }


        // Update payment information with subtotal and tax for random items
        BigInteger subTotal = transaction.getProducts().stream().map(item -> item.getQuantity().multiply(item.getPrice().toBigInteger())).reduce(BigInteger.ZERO, BigInteger::add);
        double taxRate = .05;
        double tax = taxRate * subTotal.doubleValue();
        paymentInformation.setSubTotal(subTotal.doubleValue());
        paymentInformation.setTax(tax);
        transaction.setPaymentInformation(paymentInformation);


        if (transaction.getPaymentInformation().getAddress() != null && transaction.getPaymentInformation().getAddress().getRewardsInfo() != null) {
            transaction.setRewardsInfo(transaction.getPaymentInformation().getAddress().getRewardsInfo());
        }
        if(transaction.getTransactionAction() == TransactionActionEnum.CREATED) {
            StoreEnum store;
            PurchaseChannel purchaseChannel = new PurchaseChannel();
            if (paymentInformation.getPaymentMethod().equalsIgnoreCase("cash")) {
                purchaseChannel.setChannel(PurchaseChannelEnum.IN_STORE);
                store = StoreEnum.getRandomBrickAndMortarStore();
            } else {
                if (random.nextBoolean()) {
                    purchaseChannel.setChannel(PurchaseChannelEnum.IN_STORE);
                    store = StoreEnum.getRandomBrickAndMortarStore();
                } else {
                    purchaseChannel.setChannel(PurchaseChannelEnum.ONLINE);
                    if (random.nextBoolean()) {
                        store = StoreEnum.getRandomOnlineDeliveryStores();
                        purchaseChannel.setShippingAddress(paymentInformation.getAddress());
                    } else {
                        store = StoreEnum.getRandomPickUpLocation();
                    }
                }

            }
            purchaseChannel.setStoreName(store.name());
            purchaseChannel.setStoreId(store.getStoreId());
            transaction.setPurchaseChannel(purchaseChannel);
        }

        transaction.setTimeStamp(LocalDateTime.now().toString());

        if (transaction.getTransactionAction() == TransactionActionEnum.UPDATED && transactionIdToUpdate != null) {
            // remove originally persisted transaction information
            transactionMap.put(transactionIdToUpdate, transaction);
            return transaction;
        }


        transactionMap.put(transaction.getTransactionId(), transaction);
        transactionKeys.add(transaction.getTransactionId());
        return transaction;
    }

    public static List<Item> getRandomItemsForTransaction() {
        Random random = new Random();
        int randomNumber = random.nextInt(15) + 1;
        List<Item> itemList = new ArrayList<>();
        for (int i = 0; i < randomNumber; i++) {
            itemList.add(ProductEnum.getRandomProduct());
        }
        return itemList;
    }
}
