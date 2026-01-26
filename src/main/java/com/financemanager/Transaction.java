package com.financemanager;

import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    
    private LocalDate date;
    private String description;
    private double value;
    private TransactionEnum type;
    private String payersName;
    private String beneficiarysName;

    public Transaction(LocalDate date, String description, double value, TransactionEnum type) {
        this.date = date;
        this.description = description;
        this.value = value;
        this.type = type;
    }

    public Transaction(LocalDate date, String description, double value, TransactionEnum type, String payersName, String beneficiarysName) {
        this.date = date;
        this.description = description;
        this.value = value;
        this.type = type;
        this.payersName = payersName;
        this.beneficiarysName = beneficiarysName;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getDescription() {
        return this.description;
    }

    public double getValue() {
        return this.value;
    }

    public TransactionEnum getType() {
        return this.type;
    }

    public String getPayersName() {
        return this.payersName;
    }

    public String getBeneficiarysName() {
        return this.beneficiarysName;
    }
}