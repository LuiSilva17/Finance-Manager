package com.financemanager.gui.components;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.financemanager.model.Transaction;

public class CategoryRow {
    
    public String name;
    public BigDecimal total = BigDecimal.ZERO;
    public List<Transaction> transactions = new ArrayList<>();

    public CategoryRow(String name) {
        this.name = name;
    }
    
}
