package model;

public class Address {
    private String name;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private RewardsInfo rewardsInfo;

    // Constructors (default and parameterized)

    public Address() {
    }

    public Address(String name, String street, String city, String state, String zip, String country, RewardsInfo rewardsInfo) {
        this.name = name;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
        this.rewardsInfo = rewardsInfo;
    }

    // Getter and Setter methods for each field

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // toString() method for easy debugging

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    public RewardsInfo getRewardsInfo() {
        return rewardsInfo;
    }

    public void setRewardsInfo(RewardsInfo rewardsInfo) {
        this.rewardsInfo = rewardsInfo;
    }
}
