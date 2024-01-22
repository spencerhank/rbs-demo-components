package kafka.model;

import kafka.enums.PurchaseChannelEnum;

public class PurchaseChannel {

    private PurchaseChannelEnum channel;

    private String storeName;

    private Address shippingAddress;

    public PurchaseChannelEnum getChannel() {
        return channel;
    }


    public PurchaseChannel(PurchaseChannelEnum channel, String storeName, Address shippingAddress) {
        this.channel = channel;
        this.storeName = storeName;
    }

    public PurchaseChannel() {
    }

    public void setChannel(PurchaseChannelEnum channel) {
        this.channel = channel;
    }


    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public String toString() {
        return "PurchaseChannel{" +
                "channel='" + channel.toString() + '\'' +
                ", storeName='" + storeName + '\'' +
                ", shippingAddress='" + shippingAddress + '\'' +
                '}';
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
