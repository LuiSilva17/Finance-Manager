package com.financemanager.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Transaction implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private String description;
    private BigDecimal value;
    private TransactionEnum type;
    private String payersName;
    private String beneficiarysName;

    public Transaction(LocalDate date, String description, BigDecimal value, TransactionEnum type) {
        this.date = date;
        this.description = description;
        this.value = value;
        this.type = type;
    }

    public Transaction(LocalDate date, String description, BigDecimal value, TransactionEnum type, String payersName, String beneficiarysName) {
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

    public BigDecimal getValue() {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Transaction that = (Transaction) o;

        if (this.getDate().equals(that.getDate())) {
            if (this.getDescription().equals(that.getDescription())) {
                if (this.getValue().compareTo(that.getValue()) == 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, description, value);
    }
}