package model;


import enums.PurchaseChannelEnum;

public class PurchaseChannel {

    private PurchaseChannelEnum channel;

    private String storeName;

    private String storeId;

    private Address shippingAddress;

    public PurchaseChannelEnum getChannel() {
        return channel;
    }


    public PurchaseChannel(PurchaseChannelEnum channel, String storeName, Address shippingAddress, String storeId) {
        this.channel = channel;
        this.storeName = storeName;
        this.storeId = storeId;
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

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreId() {
        return storeId;
    }

    @Override
    public String toString() {
        return "PurchaseChannel{" +
                "channel='" + channel.toString() + '\'' +
                ", storeName='" + storeName + '\'' +
                ", storeId='" + storeId + '\'' +
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
