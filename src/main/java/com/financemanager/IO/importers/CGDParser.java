package com.financemanager.IO.importers;

import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionEnum;

import java.io.*;
import java.math.BigDecimal;
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
                if (line[0].trim().isEmpty()) {
                    break;
                }

                String dateStr = line[1].replace("-", "/");
                DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(dateStr, format);
                
                String description = line[2];
                TransactionEnum type;
                BigDecimal value;
                if (!line[3].trim().isEmpty()) {
                    type = TransactionEnum.DEBIT;
                    value = new BigDecimal(line[3].replace(".", "").replace(",", ".")).negate();
                } else {
                    type = TransactionEnum.CREDIT;
                    value = new BigDecimal(line[4].replace(".", "").replace(",", "."));
                }
                Transaction transaction = new Transaction(date, description, value, type);
                list.add(transaction);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return list;
    }
    


}
