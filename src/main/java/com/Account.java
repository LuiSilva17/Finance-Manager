package com;

public class Account {
    
    private String ownersName;
    private int amount;

    public Account(String ownersName, int initialValue) {
        this.ownersName = ownersName;
        this.amount = initialValue;
    }

    public String getOwnersName() {
        return this.ownersName;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount += amount;
    }

}
