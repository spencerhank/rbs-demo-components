package kafka.enums;

import kafka.model.Address;
import kafka.model.RewardsInfo;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public enum AddressEnum {
    ADDRESS_1("John Doe", "123 Main St", "Anytown", "CA", "90210", "USA", generateRandomRewardsInfo()),
    ADDRESS_2("Alice Smith", "456 Oak Ave", "Cityville", "NY", "56789", "USA", generateRandomRewardsInfo()),
    ADDRESS_3("Bob Johnson", "789 Pine Blvd", "Villagetown", "TX", "34567", "USA", generateRandomRewardsInfo()),
    ADDRESS_4("Emma White", "321 Cedar Ln", "Countryville", "FL", "87654", "USA", generateRandomRewardsInfo()),
    ADDRESS_5("David Black", "654 Elm Rd", "Citytown", "IL", "23456", "USA", generateRandomRewardsInfo()),
    ADDRESS_6("Olivia Brown", "987 Birch Dr", "Villageville", "WA", "65432", "USA", generateRandomRewardsInfo()),
    ADDRESS_7("James Green", "135 Maple Ave", "Suburbtown", "OH", "87654", "USA", generateRandomRewardsInfo()),
    ADDRESS_8("Sophia Gray", "246 Pine Ln", "Metrotown", "GA", "12345", "USA", generateRandomRewardsInfo()),
    ADDRESS_9("Ethan White", "357 Cedar Rd", "Hometown", "MI", "54321", "USA", generateRandomRewardsInfo()),
    ADDRESS_10("Ava Black", "468 Elm Blvd", "Distantville", "PA", "98765", "USA", generateRandomRewardsInfo()),
    ADDRESS_11("Mia Brown", "579 Birch Ave", "Farawaytown", "AZ", "23456", "USA", generateRandomRewardsInfo()),
    ADDRESS_12("Noah Green", "690 Maple Dr", "Nearbyville", "CO", "87654", "USA", generateRandomRewardsInfo()),
    ADDRESS_13("Isabella Gray", "701 Pine Ln", "Outskirtstown", "UT", "76543", "USA", generateRandomRewardsInfo()),
    ADDRESS_14("William White", "812 Cedar Rd", "Remoteville", "OR", "12345", "USA", generateRandomRewardsInfo()),
    ADDRESS_15("Sophia Black", "923 Elm Blvd", "Closebytown", "KS", "56789", "USA", generateRandomRewardsInfo()),
    ADDRESS_16("Oliver Brown", "234 Birch Ave", "Adjacentville", "MN", "98765", "USA", generateRandomRewardsInfo()),
    ADDRESS_17("Emma Green", "345 Maple Dr", "Proximitytown", "NC", "23456", "USA", generateRandomRewardsInfo()),
    ADDRESS_18("Liam Gray", "456 Pine Ln", "Neighbortown", "WI", "87654", "USA", generateRandomRewardsInfo()),
    ADDRESS_19("Ava White", "567 Cedar Rd", "Adjacentville", "IA", "12345", "USA", generateRandomRewardsInfo()),
    ADDRESS_20("Jackson Black", "678 Elm Blvd", "Nearbytown", "NV", "98765", "USA", generateRandomRewardsInfo());

    private final Address address;

    AddressEnum(String name, String street, String city, String state, String zip, String country, RewardsInfo rewardsInfo) {
        this.address = new Address(name, street, city, state, zip, country, rewardsInfo);
    }

    public Address getAddress() {
        return address;
    }

    public static List<AddressEnum> getAllAddresses() {
        return Arrays.asList(values());
    }

    public static Address getRandomAddress() {
        Random random = new Random();
        return values()[random.nextInt(values().length)].getAddress();
    }

    public static RewardsInfo generateRandomRewardsInfo() {
        RewardsInfo rewardsInfo = new RewardsInfo();
        Random rand = new Random();

        int num1 = (rand.nextInt(7) + 1) * 100 + (rand.nextInt(8) * 10) + rand.nextInt(8);
        int num2 = rand.nextInt(743);
        int num3 = rand.nextInt(10000);

        DecimalFormat df3 = new DecimalFormat("000"); // 3 zeros
        DecimalFormat df4 = new DecimalFormat("0000"); // 4 zeros

        String phoneNumber = df3.format(num1) + "-" + df3.format(num2) + "-" + df4.format(num3);
        rewardsInfo.setPhoneNumber(phoneNumber);
        rewardsInfo.setMemberId(UUID.randomUUID().toString());
        return rewardsInfo;
    }
}
