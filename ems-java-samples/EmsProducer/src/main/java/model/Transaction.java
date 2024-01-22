package model;

import enums.TransactionActionEnum;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@XmlRootElement
public class Transaction implements Serializable {

    private String transactionId;

    private TransactionActionEnum transactionAction;

    private RewardsInfo rewardsInfo;

    private List<Item> items;

    private PaymentInformation paymentInformation;

    private String timeStamp;

    private PurchaseChannel purchaseChannel;


    public List<Item> getProducts() {
        return items;
    }

    public void setProducts(List<Item> items) {
        this.items = items;
    }

    public PaymentInformation getPaymentInformation() {
        return paymentInformation;
    }

    public void setPaymentInformation(PaymentInformation paymentInformation) {
        this.paymentInformation = paymentInformation;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public PurchaseChannel getPurchaseChannel() {
        return purchaseChannel;
    }

    public void setPurchaseChannel(PurchaseChannel purchaseChannel) {
        this.purchaseChannel = purchaseChannel;
    }


    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public RewardsInfo getRewardsInfo() {
        return rewardsInfo;
    }

    public void setRewardsInfo(RewardsInfo rewardsInfo) {
        this.rewardsInfo = rewardsInfo;
    }

    public TransactionActionEnum getTransactionAction() {
        return transactionAction;
    }

    public void setTransactionAction(TransactionActionEnum transactionAction) {
        this.transactionAction = transactionAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(rewardsInfo, that.rewardsInfo) && Objects.equals(items, that.items) && Objects.equals(paymentInformation, that.paymentInformation) && Objects.equals(timeStamp, that.timeStamp) && Objects.equals(purchaseChannel, that.purchaseChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, rewardsInfo, items, paymentInformation, timeStamp, purchaseChannel);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                "transactionAction='" + transactionAction + '\'' +
                ", rewardsInfo=" + rewardsInfo +
                ", items=" + items +
                ", paymentInformation=" + paymentInformation +
                ", timeStamp='" + timeStamp + '\'' +
                ", purchaseChannel=" + purchaseChannel +
                '}';
    }


}
