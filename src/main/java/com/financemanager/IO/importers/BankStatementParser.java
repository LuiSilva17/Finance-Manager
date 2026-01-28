package com.financemanager.IO.importers;

import com.financemanager.model.Transaction;

import java.io.File;
import java.util.List;

public interface BankStatementParser {
    
    public String getName();

    public List<Transaction> parse(File file);

}