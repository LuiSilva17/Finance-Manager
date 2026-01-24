package com.financemanager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class App {
    public static void main( String[] args ) {
        File file = new File("Conta Superjovem_csv.csv");
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            for (int i = 0; i < 5; i++) {
                reader.readNext();
            }
            String[] line = divideVector(reader.readNext());

            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(line[0], format);

            String description = line[2];
            int amount = Integer.parseInt(line[3]);
            int balanceAfterTransaction = Integer.parseInt(line[4]);
            int type = Integer.parseInt(line[5]);

            if (line.length > 6) {
                String payersName = line[6];
                // String payersIBAN = line[7];
                String beneficiarysName = line[8];
                Transacao transacao = new Transacao(date, description, amount, type, payersName, beneficiarysName);
            } else {
                Transacao transacao = new Transacao(date, description, balanceAfterTransaction, type);
            }



            reader.close();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public static String[] divideVector(String[] vector) {
        boolean hasExtraContent = false;
        
        for (int i = vector.length/2; i < vector.length; i++) {
            if (!vector[i].equals("")) {
                hasExtraContent = true;
            }
        }
        
        if (hasExtraContent) {
            return vector;
        } else {
            String[] v1 = new String[vector.length/2];
            for (int i = 0; i < vector.length/2; i++) {
                v1[i] = vector[i];
            }
            return v1;
        }
    }
}