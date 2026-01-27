package com.financemanager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class CSVImporter {
    
    /* This method only works for Credito Agricola since other banks might have 
    some other file format */

    public static AccountManager importTransactions(File csvFile, String managerName) {
        AccountManager manager = new AccountManager(managerName);
        try (CSVReader reader = new CSVReader(new FileReader(csvFile));) {
            for (int i = 0; i < 5; i++) {
                reader.readNext();
            }

            String[] tempLine;
            while ((tempLine = reader.readNext()) != null) {
                String[] line = divideVector(tempLine);
                if(line.length == 0) {
                    break;
                }

                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(line[0], format);

                String description = line[2];

                String cleanAmount = line[3].replace("€", "").trim(); // remove the Euro Symbol
                cleanAmount = cleanAmount.replace(".", "").replace(",", "."); // Change from 12,50 to 12.50
                double amount = Double.parseDouble(cleanAmount);
                //int balanceAfterTransaction = Integer.parseInt(line[4]);

                TransactionEnum type = TransactionEnum.DEBIT;
                if(line[5].equals("Crédito")) {
                    type = TransactionEnum.CREDT;
                }

                Transaction transaction;
                if (line.length > 6) {
                    String payersName = line[6];
                    // String payersIBAN = line[7];
                    String beneficiarysName = line[8];
                    transaction = new Transaction(date, description, amount, type, payersName, beneficiarysName);
                } else {
                    transaction = new Transaction(date, description, amount, type);
                }
                manager.addTransaction(transaction);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        } 
        return manager;
    }

    // This method only works for Credito Agricola
    public static String[] divideVector(String[] vector) {
        boolean hasExtraContent = false;
        String[] v1;

        if (vector[0].equals("")) {
            v1 = new String[0];
        } else {
            for (int i = vector.length/2; i < vector.length; i++) {
                if (!vector[i].equals("")) {
                    hasExtraContent = true;
                }
            }
            
            if (hasExtraContent) {
                return vector;
            } else {
                v1 = new String[vector.length/2];
                for (int i = 0; i < vector.length/2; i++) {
                    v1[i] = vector[i];
                }
            }
        }
        return v1;
    }
}
