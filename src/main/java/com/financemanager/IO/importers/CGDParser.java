package com.financemanager.IO.importers;

import com.financemanager.model.Transaction;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

public class CGDParser implements BankStatementParser {

    @Override
    public String getName() {
        return "Caixa Geral de Depositos";
    }

    @Override
    public List<Transaction> parse(File csvFile) {
        List<Transaction> list = new ArrayList<>();
        CSVParser csvParser = new CSVParserBuilder().withSeparator(';').build();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFile)).withCSVParser(csvParser).build();) {

            for (int i = 0; i < 8; i++) {
                reader.readNext();
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line[0].equals("")) {
                    break;
                }

                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(line[1], format);
                
                String description = line[2];
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }


        return list;
    }
    


}
