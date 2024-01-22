package enums;

import java.util.Random;

public enum TransactionActionEnum {
    CREATED,
    UPDATED,
    CANCELLED;

    TransactionActionEnum(){}


    public static TransactionActionEnum getRandomTransactionAction() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
