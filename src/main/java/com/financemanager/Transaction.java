package com.financemanager;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    
    private LocalDate date;
    private String description;
    private double value;
    private int type; // Debit = 0; Credit = 1
    private String payersName;
    private String beneficiarysName;

    public Transaction(LocalDate date, String description, double value, int type) {
        this.date = date;
        this.description = description;
        this.value = value;
        if(type != 0 || type != 1) {
            System.err.println("Type cannot be different than 0 or 1");
            System.err.println("For Debit  choose 0");
            System.err.println("For Credit choose 1");
        }
        this.type = type;
    }

    public Transaction(LocalDate date, String description, double value, int type, String payersName, String beneficiarysName) {
        this.date = date;
        this.description = description;
        this.value = value;
        if(type != 0 || type != 1) {
            System.err.println("Type cannot be different than 0 or 1");
            System.err.println("For Debit  choose 0");
            System.err.println("For Credit choose 1");
        }
        this.type = type;
        this.payersName = payersName;
        this.beneficiarysName = beneficiarysName;
    }
}