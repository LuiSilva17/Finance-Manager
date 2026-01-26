package com.financemanager;

import java.io.*;
import java.util.ArrayList;

public class AccountManager implements Serializable {
    
    private ArrayList<Transaction> transactions;
    private String name;

    public AccountManager(String name) {
        this.name = name;
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction t) {
        this.transactions.add(t);
    }

    public ArrayList<Transaction> getTransactions() {
        return this.transactions;
    }

    public String getName() {
        return this.name;
    }

    public double getCurrentBalance() {
        double total = 0.0;
        for (Transaction t : transactions) {
            total += t.getValue();
        }
        return total;
    }

    public void saveToFile(String path) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AccountManager loadFromFile(String path) {
        AccountManager manager = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            manager = (AccountManager) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return manager;
    }
}