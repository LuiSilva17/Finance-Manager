package com.financemanager.gui;

import com.financemanager.IO.importers.BankStatementParser;
import com.financemanager.IO.importers.CGDParser;
import com.financemanager.IO.importers.CreditoAgricolaParser;
import com.financemanager.data.Registry;
import com.financemanager.model.Transaction;
import com.financemanager.service.AccountManager;
import com.financemanager.service.CategoryManager;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class Menu {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Registry registry;
    private AccountManager manager;
    private boolean isEditMode = false;

    private JPanel buttonContainer;
    private JTable transactionsTable;
    private JLabel balanceLabel;
    private JLabel bankNameLabel;
    private JScrollPane dashboardScrollPane;

    private JComboBox<String> viewModeBox;

    public Menu() {
        CategoryManager.getInstance().load();

        this.registry = new Registry();
        frame = new JFrame();
        frame.setTitle("Finance Manager");
        frame.setSize(800, 500);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void start() {
        addContent();
        frame.setVisible(true);
    }

    public void addContent() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(menuPanel(), "Menu");
        mainPanel.add(createPanel(), "Create");
        mainPanel.add(loadPanel(), "Load");
        mainPanel.add(dashboardPanel(), "Dashboard");

        frame.add(mainPanel);
        cardLayout.show(mainPanel, "Menu");
    }

    // --- TELA 1: MENU PRINCIPAL ---
    private JPanel menuPanel() {
        JPanel painelGeral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel painelBotoes = new JPanel(new GridLayout(4, 1, 0, 20));

        JButton btnCreate = new JButton("Create New Manager");
        JButton btnLoad = new JButton("Load Manager");
        JButton btnManageCats = new JButton("Manage Categories");
        JButton btnExit = new JButton("Exit");

        Font font = new Font("Arial", Font.BOLD, 18);
        btnCreate.setFont(font);
        btnLoad.setFont(font);
        btnManageCats.setFont(font);
        btnExit.setFont(font);

        Dimension buttonSize = new Dimension(0, 60);
        btnCreate.setPreferredSize(buttonSize);
        btnLoad.setPreferredSize(buttonSize);
        btnManageCats.setPreferredSize(buttonSize);
        btnExit.setPreferredSize(buttonSize);

        btnCreate.addActionListener(e -> cardLayout.show(mainPanel, "Create"));
        btnLoad.addActionListener(e -> {
            refreshLoadPage();
            cardLayout.show(mainPanel, "Load");
        });

        btnManageCats.addActionListener(e -> openCategoryManager());

        btnExit.addActionListener(e -> System.exit(0));

        painelBotoes.add(btnCreate);
        painelBotoes.add(btnLoad);
        painelBotoes.add(btnManageCats);
        painelBotoes.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        painelGeral.add(Box.createGlue(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelGeral.add(painelBotoes, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        painelGeral.add(Box.createGlue(), gbc);

        return painelGeral;
    }

    // --- TELA 2: CREATE MANAGER ---
    private JPanel createPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        painelTopo.add(btnBack);
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        JPanel painelProporcional = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);
        gbcForm.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtPath = new JTextField();
        txtPath.setEditable(false);
        txtPath.setText("No file selected...");
        txtPath.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnSearch = new JButton("...");
        btnSearch.setToolTipText("Search file");
        btnSearch.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                txtPath.setText(
                    fileChooser.getSelectedFile().getAbsolutePath()
                );
            }
        });

        JButton btnCreateManager = new JButton("Create Manager");
        btnCreateManager.setFont(new Font("Arial", Font.BOLD, 18));
        btnCreateManager.setPreferredSize(new Dimension(0, 60));
        btnCreateManager.addActionListener(e -> {
            String path = txtPath.getText();
            if (path.equals("No file selected...")) {
                JOptionPane.showMessageDialog(
                    frame,
                    "Please select a file first."
                );
                return;
            }

            String[] bankOptions = { "Crédito Agrícola", "CGD", "Santander", "Novo Banco" };

            String selectedBank = (String) JOptionPane.showInputDialog(
                null,
                "What bank is this extract from?",
                "Select Bank",
                JOptionPane.QUESTION_MESSAGE,
                null,
                bankOptions,
                bankOptions[0]
            );

            // Operation was cancelled
            if (selectedBank == null) {
                return;
            }

            BankStatementParser parser = null;

            switch (selectedBank) {
                case "Crédito Agrícola":
                    parser = new CreditoAgricolaParser();
                    break;
                case "CGD":
                    parser = new CGDParser();
                    System.out.println("Ainda não implementado");
                    break;
                default:
                    parser = new CreditoAgricolaParser(); 
                    break;
            }
            
            File f = new File(path);
            String fileName = f.getName();
            int index = fileName.lastIndexOf(".");
            String trimmedName;

            if (index > 0) {
                trimmedName = fileName.substring(0, index);
            } else {
                trimmedName = fileName;
            }

            try {
                if (parser != null) {
                    List<Transaction> transactions = parser.parse(f);
                    AccountManager newManager = new AccountManager(trimmedName);
                    
                    newManager.loadTransactions(transactions);
                    newManager.saveToFile();

                    if (newManager.getFilePath() != null) {
                        registry.registerManager(trimmedName, newManager.getFilePath());
                        this.manager = newManager;
                        refreshCurrentView();
                        cardLayout.show(mainPanel, "Dashboard");
                    }
                }
            } catch (Exception excep) {
                JOptionPane.showMessageDialog(frame, "Error parsing file: " + excep.getMessage());
                excep.printStackTrace();
            }
        });

        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.weightx = 0.85;
        gbcForm.ipady = 15;
        form.add(txtPath, gbcForm);

        gbcForm.gridx = 1;
        gbcForm.weightx = 0.15;
        form.add(btnSearch, gbcForm);

        gbcForm.gridx = 0;
        gbcForm.gridy = 1;
        gbcForm.gridwidth = 2;
        gbcForm.weightx = 1.0;
        gbcForm.insets = new Insets(30, 5, 5, 5);
        form.add(btnCreateManager, gbcForm);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelProporcional.add(form, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
    }

    // --- TELA 3: LOAD MANAGER ---
    private JPanel loadPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> {
            this.isEditMode = false;
            cardLayout.show(mainPanel, "Menu");
        });

        JButton btnEdit = new JButton("Edit");
        btnEdit.setFont(new Font("Arial", Font.BOLD, 14));
        btnEdit.addActionListener(e -> {
            this.isEditMode = !this.isEditMode;
            if (this.isEditMode) {
                btnEdit.setText("Done");
            } else {
                btnEdit.setText("Edit");
            }
            refreshLoadPage();
        });

        painelTopo.add(btnBack, BorderLayout.WEST);
        painelTopo.add(btnEdit, BorderLayout.EAST);

        painelGeral.add(painelTopo, BorderLayout.NORTH);

        JPanel painelProporcional = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        this.buttonContainer = new JPanel(new GridLayout(0, 1, 0, 15));

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelProporcional.add(this.buttonContainer, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
    }

    private void refreshLoadPage() {
        this.buttonContainer.removeAll();

        if (registry.getHashMap().isEmpty()) {
            JLabel emptyMsg = new JLabel(
                "No Manager has been created yet",
                SwingConstants.CENTER
            );
            emptyMsg.setFont(new Font("Arial", Font.PLAIN, 16));
            emptyMsg.setForeground(Color.GRAY);
            emptyMsg.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

            JButton btnGoToCreate = new JButton("Create Manager");
            btnGoToCreate.setFont(new Font("Arial", Font.BOLD, 18));
            btnGoToCreate.setPreferredSize(new Dimension(0, 60));
            btnGoToCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGoToCreate.addActionListener(e ->
                cardLayout.show(mainPanel, "Create")
            );

            this.buttonContainer.add(emptyMsg);
            this.buttonContainer.add(btnGoToCreate);
            this.isEditMode = false;
        } else {
            for (Map.Entry<String, String> entry : registry
                .getHashMap()
                .entrySet()) {
                String name = entry.getKey();
                String path = entry.getValue();

                JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
                rowPanel.setOpaque(false);

                if (this.isEditMode) {
                    JButton btnRename = new JButton("✎");
                    btnRename.setFont(
                        new Font("Segoe UI Symbol", Font.PLAIN, 18)
                    );
                    btnRename.setBackground(new Color(255, 165, 0));
                    btnRename.setForeground(Color.WHITE);
                    btnRename.setFocusPainted(false);
                    btnRename.setPreferredSize(new Dimension(60, 60));

                    btnRename.addActionListener(e -> {
                        renameManagerFromMenu(name, path);
                    });

                    rowPanel.add(btnRename, BorderLayout.WEST);
                }

                JButton btnManager = new JButton(name);
                btnManager.setFont(new Font("Arial", Font.BOLD, 18));
                btnManager.setPreferredSize(new Dimension(0, 60));

                btnManager.addActionListener(e -> {
                    if (this.isEditMode) return;

                    System.out.println("Loading from: " + path);
                    this.manager = AccountManager.loadFromFile(path);
                    if (this.manager != null) {
                        this.manager.setName(name);

                        refreshCurrentView();

                        cardLayout.show(mainPanel, "Dashboard");
                    }
                });

                rowPanel.add(btnManager, BorderLayout.CENTER);

                if (this.isEditMode) {
                    JButton btnDelete = new JButton("X");
                    btnDelete.setFont(new Font("Arial", Font.BOLD, 16));
                    btnDelete.setBackground(Color.RED);
                    btnDelete.setForeground(Color.WHITE);
                    btnDelete.setFocusPainted(false);
                    btnDelete.setPreferredSize(new Dimension(60, 60));

                    btnDelete.addActionListener(e -> {
                        deleteManager(name, path);
                    });

                    rowPanel.add(btnDelete, BorderLayout.EAST);
                }

                this.buttonContainer.add(rowPanel);
            }
        }

        this.buttonContainer.revalidate();
        this.buttonContainer.repaint();
    }

    // --- TELA 4: DASHBOARD ---
    private JPanel dashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnBack = new JButton("Back to Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        topPanel.add(btnBack, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 15, 0)
        );

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
            String newName = JOptionPane.showInputDialog(
                dashboardPanel,
                "Enter new bank name:",
                currentName
            );

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

        JPanel rightActionPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

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
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);

            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                BankStatementParser parser = new CreditoAgricolaParser();
                List<Transaction> transactions = parser.parse(selectedFile);

                AccountManager tempManager = new AccountManager("Temp");
                tempManager.loadTransactions(transactions);
                if (tempManager != null && this.manager != null) {
                    this.manager.merge(tempManager);
                    this.manager.saveToFile();
                    refreshCurrentView(); 
                    JOptionPane.showMessageDialog(frame, "Transactions added successfully!");
                }
            }
        });

        JButton btnCats = new JButton("Categories");
        btnCats.setFont(new Font("Arial", Font.BOLD, 14));
        btnCats.addActionListener(e -> {
            openCategoryManager();
            refreshCurrentView();
        });

        rightActionPanel.add(viewModeBox);
        rightActionPanel.add(btnCats);
        rightActionPanel.add(btnAddFile);

        topPanel.add(rightActionPanel, BorderLayout.EAST);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);

        // --- ALTERAÇÃO: Configuração da Tabela para Suportar Ordenação de Tipos Reais ---
        String[] columnNames = { "Date", "Description", "Type", "Value" };
        Object[][] data = {};

        // Model especial que diz à tabela que a coluna 0 é DATA e a 3 é DOUBLE
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return LocalDate.class; // Para ordenar datas corretamente
                    case 3:
                        return Double.class; // Para ordenar números corretamente
                    default:
                        return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        this.transactionsTable = new JTable(model);
        this.transactionsTable.setRowHeight(30);
        this.transactionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        this.transactionsTable.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 14)
        );
        this.transactionsTable.setShowGrid(true);
        this.transactionsTable.setGridColor(Color.LIGHT_GRAY);

        // Ativar ordenação
        this.transactionsTable.setAutoCreateRowSorter(true);

        // Aplicar o Renderizador Inteligente (Pinta verde/vermelho e formata data)
        this.transactionsTable.getColumnModel()
            .getColumn(0)
            .setCellRenderer(new SmartCellRenderer());
        this.transactionsTable.getColumnModel()
            .getColumn(3)
            .setCellRenderer(new SmartCellRenderer());

        DefaultTableCellRenderer centerRenderer =
            new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        this.transactionsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        this.dashboardScrollPane = new JScrollPane(this.transactionsTable);
        dashboardPanel.add(this.dashboardScrollPane, BorderLayout.CENTER);

        return dashboardPanel;
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
                    t.getDate(), // Passa LocalDate (não String)
                    t.getDescription(),
                    typeStr,
                    t.getValue(), // Passa Double (não String)
                }
            );
        }
    }

    private void deleteManager(String name, String path) {
        int choice = JOptionPane.showConfirmDialog(
            frame, 
            "Are you sure you want to delete '" + name + "'?\nThe file will be deleted forever.", 
            "Delete Manager", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            File file = new File(path);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted: " + path);
                } else {
                    System.err.println("Failed to delete file.");
                    JOptionPane.showMessageDialog(frame, "Error: Could not delete file from disk.");
                    return;
                }
            }

            registry.getHashMap().remove(name);
            registry.save();
            refreshLoadPage();
        }
    }

    private void renameManagerFromMenu(String oldName, String path) {
        String newName = JOptionPane.showInputDialog(frame, "Rename '" + oldName + "' to:", oldName);

        if (newName != null && !newName.trim().isEmpty()) {
            String finalName = newName.trim();

            AccountManager tempManager = AccountManager.loadFromFile(path);

            if (tempManager != null) {
                tempManager.setName(finalName);
                tempManager.saveToFile();

                registry.renameManager(oldName, finalName);

                refreshLoadPage();
            } else {
                JOptionPane.showMessageDialog(frame, "Error loading file to rename.");
            }
        }
    }

    private void openCategoryManager() {
        JDialog dialog = new JDialog(frame, "Manage Categories", true);
        dialog.setSize(700, 500);
        dialog.setResizable(false);
        dialog.setLayout(new GridLayout(1, 2, 10, 0));
        dialog.setLocationRelativeTo(frame);

        CategoryManager catManager = CategoryManager.getInstance();

        DefaultListModel<String> categoryModel = new DefaultListModel<>();
        DefaultListModel<String> keywordModel = new DefaultListModel<>();

        for (String cat : catManager.getCategoriesMap().keySet()) {
            categoryModel.addElement(cat);
        }

        JList<String> categoryList = new JList<>(categoryModel);
        JList<String> keywordList = new JList<>(keywordModel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Groups"));

        JPanel leftButtons = new JPanel(new FlowLayout());
        JButton btnAddCat = new JButton("Add Category");
        JButton btnEditCat = new JButton("✎");
        JButton btnRemCat = new JButton("Remove Category");
        JButton btnInfo = new JButton("ℹ");

        leftButtons.add(btnAddCat);
        leftButtons.add(btnEditCat);
        leftButtons.add(btnRemCat);
        leftButtons.add(btnInfo);

        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        JPanel rightButtons = new JPanel(new FlowLayout());
        JButton btnAddKey = new JButton("Add Keyword");
        JButton btnEditKey = new JButton("Edit");
        JButton btnRemKey = new JButton("Remove Keyword");

        rightButtons.add(btnAddKey);
        rightButtons.add(btnEditKey);
        rightButtons.add(btnRemKey);

        rightPanel.add(new JScrollPane(keywordList), BorderLayout.CENTER);
        rightPanel.add(rightButtons, BorderLayout.SOUTH);

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCat = categoryList.getSelectedValue();
                keywordModel.clear();

                if (selectedCat != null) {
                    ArrayList<String> keys = catManager
                        .getCategoriesMap()
                        .get(selectedCat);
                    if (keys != null) {
                        for (String k : keys) {
                            keywordModel.addElement(k);
                        }
                    }
                }
            }
        });

        btnAddCat.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(
                dialog,
                "New Group Name:"
            );
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

        btnAddKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(
                    dialog,
                    "Select a Group on the left first!"
                );
                return;
            }

            String key = JOptionPane.showInputDialog(
                dialog,
                "Add keyword for " + selectedCat + ":"
            );
            if (key != null && !key.trim().isEmpty()) {
                catManager.addKeyword(selectedCat, key.trim());

                keywordModel.clear();
                for (String k : catManager
                    .getCategoriesMap()
                    .get(selectedCat)) {
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

        btnInfo.addActionListener(e -> {
            String message = "How to use Keywords:\n\n" +
                             "1. Create a Group (e.g., 'Supermarket').\n" +
                             "2. Select the group.\n" +
                             "3. Add unique words found in your bank statement description.\n" +
                             "   Example: If the statement says 'VISA PINGO DOCE 234', add 'PINGO DOCE'.\n\n" +
                             "The program will automatically link transactions containing these words to the group.";
            JOptionPane.showMessageDialog(dialog, message, "Help", JOptionPane.INFORMATION_MESSAGE);
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

                    categoryModel.removeElement(selected);
                    categoryModel.addElement(finalName);
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
                    // Ir à lista e substituir
                    ArrayList<String> keys = catManager
                        .getCategoriesMap()
                        .get(selectedCat);
                    int index = keys.indexOf(selectedKey);
                    if (index >= 0) {
                        keys.set(index, newKey.trim());

                        // Atualizar visualmente
                        keywordModel.set(
                            keywordList.getSelectedIndex(),
                            newKey.trim()
                        );
                        catManager.save();
                    }
                }
            }
        });

        dialog.add(leftPanel);
        dialog.add(rightPanel);
        dialog.setVisible(true);
    }

    private JXTreeTable buildGroupTreeTable() {
        // 1. Preparar os dados (Agrupar)
        Map<String, CategoryRow> mapRows = new HashMap<>();
        CategoryManager catManager = CategoryManager.getInstance();

        for (Transaction t : this.manager.getTransactions()) {
            String catName = catManager.getCategoryFor(t.getDescription());

            mapRows.putIfAbsent(catName, new CategoryRow(catName));

            CategoryRow row = mapRows.get(catName);
            row.transactions.add(t);
            row.total = row.total.add(t.getValue());
        }

        List<CategoryRow> categoryList = new ArrayList<>(mapRows.values());
        FinanceTreeModel model = new FinanceTreeModel(categoryList);
        JXTreeTable treeTable = new JXTreeTable(model);

        treeTable.setRowHeight(30);
        treeTable.setFont(new Font("Arial", Font.PLAIN, 14));
        treeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        treeTable.setShowGrid(true, true);
        treeTable.setGridColor(Color.LIGHT_GRAY);
        treeTable.expandAll();

        // Ativar ordenação também no modo Group
        treeTable.setAutoCreateRowSorter(true);

        treeTable.expandAll();
        treeTable.setAutoCreateRowSorter(true);

        // Usar o Renderizador Inteligente também aqui para formatar a data e o valor
        treeTable
            .getColumnModel()
            .getColumn(3)
            .setCellRenderer(new SmartCellRenderer());

        // Centralizar a coluna do Tipo (Opcional, mas tinhas pedido)
        DefaultTableCellRenderer centerRenderer =
            new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        treeTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        return treeTable;
    }

    private void refreshCurrentView() {
        if (this.manager == null || this.viewModeBox == null) return;

        String selected = (String) this.viewModeBox.getSelectedItem();

        if ("Group Mode".equals(selected)) {
            JXTreeTable groupTable = buildGroupTreeTable();
            this.dashboardScrollPane.setViewportView(groupTable);
        } else {
            updateDashboardUI();
            this.dashboardScrollPane.setViewportView(this.transactionsTable);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }

    // --- CLASSES INTERNAS AUXILIARES PARA A JXTreeTable ---

    static class CategoryRow {

        String name;
        BigDecimal total = BigDecimal.ZERO;
        List<Transaction> transactions = new ArrayList<>();

        public CategoryRow(String name) {
            this.name = name;
        }
    }

    static class FinanceTreeModel extends AbstractTreeTableModel {

        private final List<CategoryRow> categories;

        private final String[] columnNames = {
            "Date / Category",
            "Description",
            "Type",
            "Value",
        };

        public FinanceTreeModel(List<CategoryRow> categories) {
            super(new Object()); // Raiz invisível
            this.categories = categories;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 3) return Double.class;
            return String.class;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getChild(Object parent, int index) {
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
                        return t.getDescription();
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

    static class SmartCellRenderer extends DefaultTableCellRenderer {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
        ) {
            // 1. Reset básico
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
            );
            setHorizontalAlignment(JLabel.CENTER);

            // 2. Se for NÚMERO (Double)
            if (value instanceof Double) {
                double val = (Double) value;
                setText(String.format("%.2f €", val));
                updateColor(val, isSelected, table);
            }
            // 3. Se for DATA (LocalDate)
            else if (value instanceof LocalDate) {
                setText(((LocalDate) value).format(dtf));
                if (!isSelected) setForeground(Color.BLACK);
            }
            // 4. Se for TEXTO (String) - Rede de Segurança
            else if (value instanceof String) {
                String text = (String) value;
                // Tenta limpar o texto "€" e espaços para ver se é número
                try {
                    String clean = text
                        .replace("€", "")
                        .replace(",", ".")
                        .trim();
                    double val = Double.parseDouble(clean);
                    updateColor(val, isSelected, table);
                } catch (NumberFormatException e) {
                    // Não é número, fica a preto
                    if (!isSelected) setForeground(Color.BLACK);
                }
            } else {
                if (!isSelected) setForeground(Color.BLACK);
            }

            return this;
        }

        // Método auxiliar para pintar Verde/Vermelho
        private void updateColor(double val, boolean isSelected, JTable table) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
            } else {
                if (val >= 0) setForeground(new Color(0, 150, 0));
                // Verde
                else setForeground(Color.RED); // Vermelho
            }
        }
    }
}
