package com.financemanager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class AccountManager implements Serializable {
    
    private ArrayList<Transaction> list;
    private String name;

    public AccountManager(String name) {
        this.name = name;
        this.list = new ArrayList<>();
    }

    public void addTransaction(Transaction t) {
        this.list.add(t);
    }

    public ArrayList<Transaction> getTransactions() {
        return this.list;
    }

    public String getName() {
        return this.name;
    }

    public void saveToFile(String path) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
