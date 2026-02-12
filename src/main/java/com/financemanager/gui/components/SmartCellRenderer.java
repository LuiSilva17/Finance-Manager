package com.financemanager.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SmartCellRenderer extends DefaultTableCellRenderer {
    
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        setHorizontalAlignment(JLabel.CENTER);

        if (value instanceof Double) {
            double val = (Double) value;
            setText(String.format("%.2f €", val));
            updateColor(val, isSelected, table);
            
        } else if (value instanceof LocalDate) {
            setText(((LocalDate) value).format(dtf));
            if (!isSelected) setForeground(Color.BLACK);
            
        } else if (value instanceof String) {
            // Tenta converter Strings que pareçam dinheiro
            String text = (String) value;
            try {
                String clean = text
                    .replace("€", "")
                    .replace(",", ".")
                    .trim();
                double val = Double.parseDouble(clean);
                // Se conseguir converter, aplica a cor
                updateColor(val, isSelected, table); 
            } catch (NumberFormatException e) {
                // Se for só texto normal
                if (!isSelected) setForeground(Color.BLACK);
            }
        } else {
            if (!isSelected) setForeground(Color.BLACK);
        }
        
        return this;
    }

    private void updateColor(double val, boolean isSelected, JTable table) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
        } else {
            if (val >= 0) {
                setForeground(new Color(0, 150, 0)); // Verde escuro
            } else {
                setForeground(Color.RED); 
            }
        }
    }
}