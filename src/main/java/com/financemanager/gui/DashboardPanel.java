package com.financemanager.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.calendar.DateSelectionModel;

import com.financemanager.IO.importers.BankStatementParser;
import com.financemanager.IO.importers.CGDParser;
import com.financemanager.IO.importers.CreditoAgricolaParser;
import com.financemanager.gui.Menu.CategoryRow;
import com.financemanager.gui.Menu.FinanceTreeModel;
import com.financemanager.gui.Menu.SmartCellRenderer;
import com.financemanager.model.Transaction;
import com.financemanager.service.AccountManager;
import com.financemanager.service.CategoryManager;
import com.financemanager.data.Registry;

public class DashboardPanel extends JPanel {

    private AccountManager manager;
    private Registry registry;

    //private JFrame frame;
    private JTable transactionsTable;
    private JLabel bankNameLabel;
    private JLabel balanceLabel;
    private JComboBox viewModeBox;
    private JScrollPane dashboardScrollPane;

    private LocalDate filterStartDate = null;
    private LocalDate filterEndDate = null;
    private JLabel filterLabel;
    
    public DashboardPanel(CardLayout cardLayout, JPanel mainPanel) {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnBack = new JButton("Back to Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        topPanel.add(btnBack, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        this.bankNameLabel = new JLabel("Bank Name");
        this.bankNameLabel.setFont(new Font("Arial", Font.BOLD, 22));

        JButton editButton = new JButton("✎");
        editButton.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        editButton.setToolTipText("Edit Name");
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        editButton.addActionListener(e -> {
            if (this.manager == null) return;
            String currentName = this.manager.getName();
            String newName = JOptionPane.showInputDialog(dashboardPanel, "Enter new bank name:", currentName);

            if (newName != null && !newName.trim().isEmpty()) {
                String finalName = newName.trim();
                this.manager.setName(finalName);
                this.bankNameLabel.setText(finalName);
                this.manager.saveToFile();
                this.registry.renameManager(currentName, finalName);
            }
        });

        this.balanceLabel = new JLabel("0.00 €");
        this.balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        this.balanceLabel.setForeground(new Color(0, 100, 0));

        titlePanel.add(this.bankNameLabel);
        titlePanel.add(editButton);
        titlePanel.add(new JLabel("|"));
        titlePanel.add(this.balanceLabel);

        topPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel rightActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        String[] modes = { "Default Mode", "Group Mode" };
        this.viewModeBox = new JComboBox<>(modes);
        this.viewModeBox.setFont(new Font("Arial", Font.BOLD, 14));
        this.viewModeBox.setFocusable(false);
        this.viewModeBox.setBackground(Color.WHITE);
        this.viewModeBox.addActionListener(e -> refreshCurrentView());

        JButton btnAddFile = new JButton("+ Add File");
        btnAddFile.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddFile.setBackground(new Color(230, 230, 250));
        btnAddFile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAddFile.addActionListener(e -> {
            if (this.manager.getBankType() == null) {
                String[] bankOptions = { "Crédito Agrícola", "CGD", "Santander", "Novo Banco" };
                String selectedBank = (String) JOptionPane.showInputDialog(
                    this,
                    "This Manager has no Bank Type assigned (Old Version).\nPlease select the bank to fix this:",
                    "Fix Legacy Manager",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    bankOptions,
                    bankOptions[0]
                );
                if (selectedBank == null) return;
                this.manager.setBankType(selectedBank);
                this.manager.saveToFile();
            }
            
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                BankStatementParser parser = null;
                String bankType = this.manager.getBankType(); 

                switch (bankType) {
                    case "Crédito Agrícola": parser = new CreditoAgricolaParser(); break;
                    case "CGD": parser = new CGDParser(); break;
                    default: parser = new CreditoAgricolaParser(); break;
                }

                try {
                    List<Transaction> newTransactions = parser.parse(selectedFile);
                    this.manager.mergeTransactions(newTransactions);
                    this.manager.saveToFile();
                    refreshCurrentView();
                    JOptionPane.showMessageDialog(this, "Success! Transactions added.");
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Error reading file.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton btnCategories = new JButton("Categories");
        btnCategories.setFont(new Font("Arial", Font.BOLD, 14));
        btnCategories.addActionListener(e -> {
            openCategoryManager();
            refreshCurrentView();
        });

        // REPLACEMENT: Use custom component instead of basic JButton
        JPanel dateFilterPanel = createDateFilterComponent();

        rightActionPanel.add(viewModeBox);
        rightActionPanel.add(dateFilterPanel);
        rightActionPanel.add(btnCategories);
        rightActionPanel.add(btnAddFile);
        topPanel.add(rightActionPanel, BorderLayout.EAST);
        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        
        String[] columnNames = { "Date", "Description", "Type", "Value" };
        Object[][] data = {};

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return LocalDate.class;
                    case 3: return Double.class;
                    default: return String.class;
                }
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        this.transactionsTable = new JTable(model) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int viewRow = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                if (colIndex == 1 && viewRow >= 0 && viewModeBox.getSelectedItem().equals("Default Mode")) {
                    int modelRow = convertRowIndexToModel(viewRow);
                    if (manager != null && modelRow < manager.getTransactions().size()) {
                        Transaction t = manager.getTransactions().get(modelRow);
                        if (!t.getDescription().equals(t.getDisplayDescription())) {
                            tip = "Original: " + t.getDescription();
                        }
                    }
                }
                return tip;
            }
        };

        this.transactionsTable.setRowHeight(30);
        this.transactionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        this.transactionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        this.transactionsTable.setShowGrid(true);
        this.transactionsTable.setGridColor(Color.LIGHT_GRAY);
        this.transactionsTable.setAutoCreateRowSorter(true);

        this.transactionsTable.getColumnModel().getColumn(0).setCellRenderer(new SmartCellRenderer());
        this.transactionsTable.getColumnModel().getColumn(3).setCellRenderer(new SmartCellRenderer());
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        this.transactionsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // --- LISTENER MOUSE SIMPLIFIED ---
        this.transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { showPopup(e); }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { showPopup(e); }

            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger() && viewModeBox.getSelectedItem().equals("Default Mode")) {
                    int viewRow = transactionsTable.rowAtPoint(e.getPoint());
                    if (viewRow >= 0 && viewRow < transactionsTable.getRowCount()) {
                        transactionsTable.setRowSelectionInterval(viewRow, viewRow);
                        
                        // Get real transaction considering sorting
                        int modelRow = transactionsTable.convertRowIndexToModel(viewRow);
                        Transaction t = manager.getTransactions().get(modelRow);
                        
                        showTransactionPopup(e.getComponent(), e.getX(), e.getY(), t);
                    }
                }
            }
        });

        this.dashboardScrollPane = new JScrollPane(this.transactionsTable);
        dashboardPanel.add(this.dashboardScrollPane, BorderLayout.CENTER);

        //return dashboardPanel;
    }

    private void refreshCurrentView() {
        if (this.manager == null) return;

        this.bankNameLabel.setText(this.manager.getName());

        List<Transaction> currentList = getFilteredTransactions();

        // Update Balance Label
        if (this.filterStartDate != null || this.filterEndDate != null) {
            BigDecimal filteredTotal = BigDecimal.ZERO;
            for (Transaction t : currentList) {
                filteredTotal = filteredTotal.add(t.getValue());
            }
            this.balanceLabel.setText("Filter: " + String.format("%.2f €", filteredTotal));
            this.balanceLabel.setForeground(Color.BLUE.darker());
        } else {
            BigDecimal total = this.manager.getCurrentBalance();
            this.balanceLabel.setText(String.format("%.2f €", total));
            
            if (total.compareTo(BigDecimal.ZERO) >= 0) {
                this.balanceLabel.setForeground(new Color(0, 100, 0));
            } else {
                this.balanceLabel.setForeground(Color.RED);
            }
        }

        String mode = (String) this.viewModeBox.getSelectedItem();

        if ("Group Mode".equals(mode)) {
            // --- GROUP MODE ---
            JXTreeTable treeTable = buildGroupTreeTable();
            this.dashboardScrollPane.setViewportView(treeTable);
            
        } else {
            // --- DEFAULT MODE ---
            this.dashboardScrollPane.setViewportView(this.transactionsTable);

            DefaultTableModel model = (DefaultTableModel) this.transactionsTable.getModel();
            model.setRowCount(0);

            for (Transaction t : currentList) {
                Object[] row = new Object[4];
                row[0] = t.getDate();
                row[1] = t.getDisplayDescription(); 
                row[2] = t.getType();
                row[3] = t.getValue();
                model.addRow(row);
            }
        }
    }

    private JXTreeTable buildGroupTreeTable() {
        Map<String, CategoryRow> mapRows = new HashMap<>();
        CategoryManager catManager = CategoryManager.getInstance();

        // 1. Group Transactions
        for (Transaction t : getFilteredTransactions()) {
            String catName = t.getEffectiveCategory(); 
            mapRows.putIfAbsent(catName, new CategoryRow(catName));
            CategoryRow row = mapRows.get(catName);
            row.transactions.add(t);
            row.total = row.total.add(t.getValue());
        }

        // 2. Sort
        List<CategoryRow> sortedList = new ArrayList<>();
        List<String> order = catManager.getOrderedCategories();

        if (order != null) {
            for (String name : order) {
                if (name.equals("Uncategorized") || name.equals("Sem Categoria")) continue;
                CategoryRow row = mapRows.get(name);
                if (row != null) {
                    sortedList.add(row);
                    mapRows.remove(name);
                }
            }
        }

        List<String> remainingKeys = new ArrayList<>(mapRows.keySet());
        for (String key : remainingKeys) {
            if (key.equals("Uncategorized") || key.equals("Sem Categoria")) continue;
            sortedList.add(mapRows.get(key));
        }

        if (mapRows.containsKey("Uncategorized")) sortedList.add(mapRows.get("Uncategorized"));
        if (mapRows.containsKey("Sem Categoria")) sortedList.add(mapRows.get("Sem Categoria"));

        // 3. Create Table
        FinanceTreeModel model = new FinanceTreeModel(sortedList);
        JXTreeTable treeTable = new JXTreeTable(model);

        treeTable.setRowHeight(30);
        treeTable.setFont(new Font("Arial", Font.PLAIN, 14));
        treeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        treeTable.setShowGrid(true, true);
        treeTable.setGridColor(Color.LIGHT_GRAY);
        treeTable.expandAll();
        treeTable.setAutoCreateRowSorter(true);

        treeTable.getColumnModel().getColumn(3).setCellRenderer(new SmartCellRenderer());
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        treeTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // --- MOUSE LISTENER ---
        treeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { showPopup(e); }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { showPopup(e); }

            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = treeTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        treeTable.setRowSelectionInterval(row, row);
                        Object node = treeTable.getPathForRow(row).getLastPathComponent();
                        
                        if (node instanceof Transaction) {
                            showTransactionPopup(e.getComponent(), e.getX(), e.getY(), (Transaction) node);
                        }
                    }
                }
            }
        });

        return treeTable;
    }

    private void updateDashboardUI() {
        if (this.manager == null) {
            return;
        }

        this.bankNameLabel.setText(this.manager.getName());

        BigDecimal saldo = this.manager.getCurrentBalance();

        balanceLabel.setText(String.format("%.2f €", saldo));

        if (saldo.compareTo(BigDecimal.ZERO)  >= 0) {
            balanceLabel.setForeground(Color.GREEN.darker());
        } else {
            balanceLabel.setForeground(Color.RED);
        }

        DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
        model.setRowCount(0); 

        for (Transaction t : this.manager.getTransactions()) {
            String typeStr = t.getValue().compareTo(BigDecimal.ZERO) >= 0 ? "Credit" : "Debit";
            model.addRow(
                new Object[] {
                    t.getDate(), 
                    t.getDescription(),
                    typeStr,
                    t.getValue(),
                }
            );
        }
    }

    private void openCategoryManager() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "Manage Categories", true);
        dialog.setSize(750, 500);
        dialog.setResizable(false);
        dialog.setLayout(new GridLayout(1, 2, 10, 0));
        dialog.setLocationRelativeTo(this);

        CategoryManager catManager = CategoryManager.getInstance();
        catManager.syncOrder(); 

        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        DefaultListModel<String> keywordModel = new DefaultListModel<>();

        for (String cat : catManager.getOrderedCategories()) {
            categoryModel.addElement(cat);
        }

        JList<String> categoryList = new JList<>(categoryModel);
        JList<String> keywordList = new JList<>(keywordModel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        JPanel leftButtons = new JPanel(new FlowLayout());
        JButton btnAddCat = new JButton("Add");
        JButton btnEditCat = new JButton("Edit");
        JButton btnRemCat = new JButton("Remove");
        JButton btnUp = new JButton("⬆");
        JButton btnDown = new JButton("⬇");

        leftButtons.add(btnAddCat);
        leftButtons.add(btnEditCat);
        leftButtons.add(btnRemCat);
        leftButtons.add(btnUp);
        leftButtons.add(btnDown);

        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        JPanel rightButtons = new JPanel(new FlowLayout());
        JButton btnAddKey = new JButton("Add");
        JButton btnEditKey = new JButton("Edit");
        JButton btnRemKey = new JButton("Remove");
        JButton btnInfo = new JButton("ℹ");

        rightButtons.add(btnAddKey);
        rightButtons.add(btnEditKey);
        rightButtons.add(btnRemKey);
        rightButtons.add(btnInfo);

        rightPanel.add(new JScrollPane(keywordList), BorderLayout.CENTER);
        rightPanel.add(rightButtons, BorderLayout.SOUTH);

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCat = categoryList.getSelectedValue();
                keywordModel.clear();

                if (selectedCat != null) {
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selectedCat);
                    if (keys != null) {
                        for (String k : keys) {
                            keywordModel.addElement(k);
                        }
                    }
                }
            }
        });

        btnAddCat.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(dialog, "New Group Name:");
            if (name != null && !name.trim().isEmpty()) {
                String finalName = name.trim();
                if (!catManager.getCategoriesMap().containsKey(finalName)) {
                    catManager.addCategory(finalName);
                    categoryModel.addElement(finalName);
                    catManager.save();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Group already exists!");
                }
            }
        });

        btnRemCat.addActionListener(e -> {
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Delete group '" + selected + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    catManager.getCategoriesMap().remove(selected);
                    categoryModel.removeElement(selected);
                    keywordModel.clear();
                    catManager.save();
                }
            }
        });

        btnUp.addActionListener(e -> {
            int index = categoryList.getSelectedIndex();
            String selected = categoryList.getSelectedValue();
            
            if (index > 0 && selected != null) {
                catManager.moveCategoryUp(selected);
                
                String temp = categoryModel.get(index - 1);
                categoryModel.set(index - 1, selected);
                categoryModel.set(index, temp);
                
                categoryList.setSelectedIndex(index - 1);
                catManager.save();
            }
        });

        btnDown.addActionListener(e -> {
            int index = categoryList.getSelectedIndex();
            String selected = categoryList.getSelectedValue();
            
            if (index >= 0 && index < categoryModel.getSize() - 1 && selected != null) {
                catManager.moveCategoryDown(selected);

                String temp = categoryModel.get(index + 1);
                categoryModel.set(index + 1, selected);
                categoryModel.set(index, temp);
                
                categoryList.setSelectedIndex(index + 1);
                catManager.save();
            }
        });

        btnAddKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(dialog, "Select a Group on the left first!");
                return;
            }

            String key = JOptionPane.showInputDialog(dialog, "Add keyword for " + selectedCat + ":");
            if (key != null && !key.trim().isEmpty()) {
                catManager.addKeyword(selectedCat, key.trim());
                keywordModel.clear();
                for (String k : catManager.getCategoriesMap().get(selectedCat)) {
                    keywordModel.addElement(k);
                }
                catManager.save();
            }
        });

        btnRemKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            String selectedKey = keywordList.getSelectedValue();

            if (selectedCat != null && selectedKey != null) {
                catManager.getCategoriesMap().get(selectedCat).remove(selectedKey);
                keywordModel.removeElement(selectedKey); 
                catManager.save();
            }
        });

        btnEditCat.addActionListener(e -> {
            String selected = categoryList.getSelectedValue();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(dialog, "Rename '" + selected + "' to:", selected);
                if (newName != null && !newName.trim().isEmpty()) {
                    String finalName = newName.trim();
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selected);
                    catManager.getCategoriesMap().remove(selected);
                    catManager.getCategoriesMap().put(finalName, keys);
                    
                    int index = categoryList.getSelectedIndex();
                    categoryModel.set(index, finalName);
                    catManager.save();
                }
            }
        });

        btnEditKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            String selectedKey = keywordList.getSelectedValue();

            if (selectedCat != null && selectedKey != null) {
                String newKey = JOptionPane.showInputDialog(dialog, "Rename keyword:", selectedKey);
                if (newKey != null && !newKey.trim().isEmpty()) {
                    ArrayList<String> keys = catManager.getCategoriesMap().get(selectedCat);
                    int index = keys.indexOf(selectedKey);
                    if (index >= 0) {
                        keys.set(index, newKey.trim());
                        keywordModel.set(keywordList.getSelectedIndex(), newKey.trim());
                        catManager.save();
                    }
                }
            }
        });

        btnInfo.addActionListener(e -> {
             JOptionPane.showMessageDialog(dialog, "Select a category and add keywords found in bank descriptions.", "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(leftPanel);
        dialog.add(rightPanel);
        dialog.setVisible(true);
    }

    private void showTransactionPopup(Component component, int x, int y, Transaction t) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Description ✏️");
        JMenuItem categoryItem = new JMenuItem("Set Category 🏷️");
        popupMenu.add(editItem);
        popupMenu.add(categoryItem);

        // ACTION 1: EDIT DESCRIPTION
        editItem.addActionListener(ev -> {
            String newNote = (String) JOptionPane.showInputDialog(
                    this, "Edit Description:", "Edit Transaction", 
                    JOptionPane.PLAIN_MESSAGE, null, null, t.getDisplayDescription()
            );
            if (newNote != null) {
                t.setUserNote(newNote);
                manager.saveToFile();
                refreshCurrentView(); 
            }
        });

        // ACTION 2: MANUAL CATEGORY OVERRIDE
        categoryItem.addActionListener(ev -> {
            List<String> cats = new ArrayList<>(CategoryManager.getInstance().getCategoriesList());
            String addNewOption = "[ + Add new Category... ]";
            cats.add(addNewOption);
            
            String selectedCat = (String) JOptionPane.showInputDialog(
                this, "Manually assign category to this specific transaction:", "Set Category Override",
                JOptionPane.QUESTION_MESSAGE, null, cats.toArray(), t.getEffectiveCategory()
            );

            if (selectedCat != null) {
                if (selectedCat.equals(addNewOption)) {
                    String newCatName = JOptionPane.showInputDialog(this, "Enter name for the new Category:");
                    if (newCatName != null && !newCatName.trim().isEmpty()) {
                        CategoryManager.getInstance().addCategory(newCatName.trim());
                        CategoryManager.getInstance().save();
                        t.setCategory(newCatName.trim()); 
                    }
                } else {
                    t.setCategory(selectedCat);
                }
                manager.saveToFile();
                refreshCurrentView();
            }
        });

        popupMenu.show(component, x, y);
    }

    private String getFilterLabelText() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (filterStartDate == null && filterEndDate == null) {
            return "Today: " + LocalDate.now().format(fmt); 
        } else {
            if (filterStartDate.equals(filterEndDate)) {
                return filterStartDate.format(fmt);
            } else {
                return filterStartDate.format(fmt) + " - " + filterEndDate.format(fmt);
            }
        }
    }

    private List<Transaction> getFilteredTransactions() {
        if (this.manager == null) return new ArrayList<>();
        
        if (filterStartDate == null && filterEndDate == null) {
            return this.manager.getTransactions();
        }

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : this.manager.getTransactions()) {
            LocalDate d = t.getDate();
            
            boolean isAfterStart = (filterStartDate == null) || (!d.isBefore(filterStartDate));
            boolean isBeforeEnd = (filterEndDate == null) || (!d.isAfter(filterEndDate));

            if (isAfterStart && isBeforeEnd) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    private JPanel createDateFilterComponent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setPreferredSize(new Dimension(220, 30));

        // Label com o texto da data
        this.filterLabel = new JLabel(getFilterLabelText());
        this.filterLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        this.filterLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        // Seta para parecer um dropdown
        JButton arrowBtn = new JButton("▼");
        arrowBtn.setFont(new Font("Arial", Font.PLAIN, 10));
        arrowBtn.setFocusable(false);
        arrowBtn.setBorderPainted(false);
        arrowBtn.setContentAreaFilled(false);
        
        // Clicar na seta abre o popup
        arrowBtn.addActionListener(e -> showDateFilterPopup(panel));

        panel.add(this.filterLabel, BorderLayout.CENTER);
        panel.add(arrowBtn, BorderLayout.EAST);

        // Clicar no painel branco também abre o popup
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showDateFilterPopup(panel);
            }
        });

        return panel;
    }

    private void showDateFilterPopup(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.setLayout(new BorderLayout());
        popup.setBackground(Color.WHITE);

        JXMonthView monthView = new JXMonthView();
        monthView.setTraversable(true);
        monthView.setSelectionMode(DateSelectionModel.SelectionMode.SINGLE_INTERVAL_SELECTION);
        
        // Configuração visual do calendário
        monthView.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Carregar seleção existente
        if (this.filterStartDate != null) {
            Date start = Date.from(this.filterStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = (this.filterEndDate != null) 
                ? Date.from(this.filterEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                : start;
            monthView.setSelectionInterval(start, end);
            monthView.ensureDateVisible(start);
        } else {
            monthView.ensureDateVisible(new Date());
        }
        
        // 1. Definir a âncora inicial (se já houver filtro, é o inicio do filtro)
        final Date[] anchorDate = { (this.filterStartDate != null) ? Date.from(this.filterStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null };

        monthView.addActionListener(e -> {
            boolean isShiftDown = (e.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) != 0;
            
            if (isShiftDown && anchorDate[0] != null) {
                // LÓGICA MANUAL (Só interfere se o SHIFT estiver pressionado)
                // Isto corrige o bug de selecionar entre meses diferentes
                Date clickedDate = monthView.getSelectionDate();
                if (clickedDate != null) {
                    Date start = clickedDate.before(anchorDate[0]) ? clickedDate : anchorDate[0];
                    Date end = clickedDate.before(anchorDate[0]) ? anchorDate[0] : clickedDate;
                    monthView.setSelectionInterval(start, end);
                }
            } else {
                // COMPORTAMENTO PADRÃO (Clique normal ou Arrastar)
                // Não forçamos nada aqui! Deixamos o JXMonthView gerir o arrasto nativamente.
                // Apenas atualizamos a âncora para o caso de o utilizador querer usar Shift a seguir.
                Date first = monthView.getFirstSelectionDate();
                if (first != null) {
                    anchorDate[0] = first; 
                }
            }
        });

        // --- PAINEL DE BOTÕES ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnClear = new JButton("Clear");
        JButton btnApply = new JButton("Apply");
        
        Dimension btnSize = new Dimension(70, 25);
        btnClear.setPreferredSize(btnSize);
        btnApply.setPreferredSize(btnSize);

        btnClear.addActionListener(e -> {
            this.filterStartDate = null;
            this.filterEndDate = null;
            updateFilterLabel();
            refreshCurrentView();
            popup.setVisible(false);
        });

        btnApply.addActionListener(e -> {
            Date start = monthView.getFirstSelectionDate();
            Date end = monthView.getLastSelectionDate();
            if (start != null) {
                this.filterStartDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                this.filterEndDate = (end != null) ? end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : this.filterStartDate;
            } else {
                this.filterStartDate = null;
                this.filterEndDate = null;
            }
            updateFilterLabel();
            refreshCurrentView();
            popup.setVisible(false);
        });

        btnPanel.add(btnClear);
        btnPanel.add(btnApply);

        popup.add(monthView, BorderLayout.CENTER);
        popup.add(btnPanel, BorderLayout.SOUTH);

        popup.show(invoker, 0, invoker.getHeight());
    }

    private void updateFilterLabel() {
        if (this.filterLabel != null) {
            this.filterLabel.setText(getFilterLabelText());
            JPanel parent = (JPanel) this.filterLabel.getParent();
            if (filterStartDate != null) {
                parent.setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
            } else {
                parent.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            }
        }
    }

    public void setManager(AccountManager newManager) {
        this.manager = newManager;
    }

    public void setRegistry(Registry newRegistry) {
        this.registry = newRegistry;
    }

}
