package com.financemanager;

import java.time.LocalDate;

public class Transacao {
    
    private LocalDate date;
    private String description;
    private int value;
    private int type; // Debit = 0; Credit = 1
    private String payersName;
    private String beneficiarysName;

    public Transacao(LocalDate date, String description, int value, int type) {
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

    public Transacao(LocalDate date, String description, int value, int type, String payersName, String beneficiarysName) {
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