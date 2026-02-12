package com.financemanager.gui.components;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List; // <--- FALTAVA ISTO

import org.jdesktop.swingx.treetable.AbstractTreeTableModel; // <--- FALTAVA ISTO

import com.financemanager.model.Transaction;

public class FinanceTreeModel extends AbstractTreeTableModel { 
    
    private final List<CategoryRow> categories;

    private final String[] columnNames = {
        "Date / Category",
        "Description",
        "Type",
        "Value",
    };

    public FinanceTreeModel(List<CategoryRow> categories) {
        // Define um objeto genérico como raiz (root), pois as categorias são os filhos imediatos
        super(new Object()); 
        this.categories = categories;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 3) return Double.class; // Para ordenar valores corretamente
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getChild(Object parent, int index) {
        // 'root' agora existe porque estendemos AbstractTreeTableModel
        if (parent == root) {
            return categories.get(index);
        }
        if (parent instanceof CategoryRow) {
            return ((CategoryRow) parent).transactions.get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == root) {
            return categories.size();
        }
        if (parent instanceof CategoryRow) {
            return ((CategoryRow) parent).transactions.size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof Transaction;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == root && child instanceof CategoryRow) {
            return categories.indexOf(child);
        }
        if (parent instanceof CategoryRow && child instanceof Transaction) {
            return ((CategoryRow) parent).transactions.indexOf(child);
        }
        return -1;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (node instanceof CategoryRow) {
            CategoryRow cat = (CategoryRow) node;
            switch (column) {
                case 0:
                    return cat.name;
                case 3:
                    return cat.total;
                default:
                    return "";
            }
        }

        if (node instanceof Transaction) {
            Transaction t = (Transaction) node;
            String typeStr = t.getValue().compareTo(BigDecimal.ZERO) >= 0 ? "Credit" : "Debit";

            switch (column) {
                case 0:
                    return t.getDate().format(dtf);
                case 1:
                    return t.getDisplayDescription();
                case 2:
                    return typeStr;
                case 3:
                    return t.getValue();
                default:
                    return "";
            }
        }
        return "";
    }
}