package com.financemanager.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SmartCellRenderer extends DefaultTableCellRenderer {
    
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // 1. Deixa o Swing tratar do básico (seleção, cores padrão)
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // 2. ZEBRA STRIPES: Alternar cores de fundo para não cansar a vista
        if (!isSelected) {
            if (row % 2 == 0) {
                setBackground(Color.WHITE);
            } else {
                setBackground(new Color(245, 245, 245)); // Cinza muito suave
            }
        }

        // 3. ALINHAMENTO DINÂMICO: A regra de ouro da legibilidade
        // Ordem: 0:Date, 1:Type, 2:Value, 3:Category, 4:Description
        switch (column) {
            case 0: // Date
            case 1: // Type
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(null);
                break;
                
            case 2: // Value
                setHorizontalAlignment(JLabel.RIGHT);
                // Pequena margem à direita para o valor não colar no limite da célula
                setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 
                break;
                
            case 3: // Category
            case 4: // Description
                setHorizontalAlignment(JLabel.LEFT);
                // Margem à esquerda para o texto não colar na linha
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                break;
        }

        // 4. LÓGICA DE DADOS E CORES (O teu motor de processamento)
        if (value instanceof Double || value instanceof java.math.BigDecimal) {
            double val = ((Number) value).doubleValue();
            setText(String.format("%.2f €", val));
            updateColor(val, isSelected, table);
            
        } else if (value instanceof LocalDate) {
            setText(((LocalDate) value).format(dtf));
            if (!isSelected) setForeground(Color.BLACK);
            
        } else if (value instanceof String) {
            String text = (String) value;
            try {
                // Tenta converter se a string parecer dinheiro (ex: vindo de outros parsers)
                String clean = text.replace("€", "").replace(",", ".").trim();
                double val = Double.parseDouble(clean);
                updateColor(val, isSelected, table); 
            } catch (NumberFormatException e) {
                // Texto normal (Categorias/Descrições)
                if (!isSelected) setForeground(new Color(50, 50, 50)); // Cinza escuro, mais suave que preto puro
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