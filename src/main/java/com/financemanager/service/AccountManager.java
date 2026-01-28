package com.financemanager.service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.financemanager.model.Transaction;

public class AccountManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Transaction> transactions;
    private String name;
    private String filePath;

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

    public void loadTransactions(List<Transaction> list) {
        if (list != null) {
            this.transactions.addAll(list);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCurrentBalance() {
        double total = 0.0;
        for (Transaction t : transactions) {
            total += t.getValue();
        }
        return total;
    }

    public void saveToFile() {
        if (this.filePath == null) {
            String dataPath =
                System.getProperty("user.home") +
                File.separator +
                "Finance Manager Data";
            File directory = new File(dataPath);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            this.filePath = dataPath + File.separator + this.name + ".manager";
        }

        try (
            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(this.filePath)
            )
        ) {
            out.writeObject(this);
            System.out.println("File saved to: " + this.filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public static AccountManager loadFromFile(String path) {
        AccountManager manager = null;
        try (
            ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(path)
            )
        ) {
            manager = (AccountManager) in.readObject();
            manager.setFilePath(path);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return manager;
    }

    public void merge(AccountManager tempManager) {
        if (tempManager != null && tempManager.getTransactions() != null) {
            this.transactions.addAll(tempManager.getTransactions());
        }
    }
}
