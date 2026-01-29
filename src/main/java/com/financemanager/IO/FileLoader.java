package com.financemanager.IO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileLoader {
    
    public static List<String> readLines(File file, char separator) throws IOException {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".csv")) {
            // Se for CSV, lê o ficheiro original (ignora o separador pedido, usa o que está lá)
            return Files.readAllLines(file.toPath());
        } 
        else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            // Se for Excel, passamos o separador para a conversão
            return readExcelAsCsvLines(file, separator);
        }
        
        throw new IOException("Unsupported file format: " + fileName);
    }

    private static List<String> readExcelAsCsvLines(File file, char separator) throws IOException {
        List<String> lines = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                List<String> cellValues = new ArrayList<>();
                int maxCell = row.getLastCellNum();
                for (int i = 0; i < maxCell; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cellValues.add(getCellValueAsString(cell));
                }

                lines.add(String.join(String.valueOf(separator), cellValues));
            }
        }
        return lines;
    }

    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Formata datas se necessário, ou devolve raw
                    return cell.getDateCellValue().toString();
                } else {
                    // Evita notação científica em números (ex: 1.2E3)
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) return String.format("%d", (long) val);
                    else return String.valueOf(val).replace(".", ","); // PT usa vírgula
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
}
