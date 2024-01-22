package model;

public class RewardsInfo {

    private String phoneNumber;

    public RewardsInfo() {
    }

    private String memberId;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public RewardsInfo(String phoneNumber, String memberId) {
        this.phoneNumber = phoneNumber;
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return "RewardsInfo{" +
                "phoneNumber='" + phoneNumber + '\'' +
//                ", memberId='" + memberId + '\'' +
                '}';
    }
}
