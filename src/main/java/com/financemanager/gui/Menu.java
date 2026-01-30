package com.financemanager.gui;

import com.financemanager.IO.importers.BankStatementParser;
import com.financemanager.IO.importers.CGDParser;
import com.financemanager.IO.importers.CreditoAgricolaParser;
import com.financemanager.data.Registry;
import com.financemanager.model.Transaction;
import com.financemanager.service.AccountManager;
import com.financemanager.service.CategoryManager;
import com.financemanager.service.SettingsManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.calendar.DateSelectionModel;
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

    // Filter Variables
    private LocalDate filterStartDate = null;
    private LocalDate filterEndDate = null;
    private JLabel filterLabel; // Label inside the custom date button

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }

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

    // --- SCREEN 1: MAIN MENU ---
    private JPanel menuPanel() {
        JPanel rootPanel = new JPanel(new BorderLayout());

        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnSettings = new JButton("⚙️");
        btnSettings.setToolTipText("Settings / Install Parsers");
        btnSettings.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnSettings.setFocusable(false);
        btnSettings.addActionListener(e -> openSettings());

        painelTopo.add(btnSettings);
        rootPanel.add(painelTopo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel painelBotoes = new JPanel(new GridLayout(5, 1, 0, 20));

        JButton btnCreate = new JButton("Create New Manager");
        JButton btnLoad = new JButton("Load Manager");
        JButton btnImport = new JButton("Import Existing Manager");
        JButton btnManageCats = new JButton("Manage Categories");
        JButton btnExit = new JButton("Exit");

        Font font = new Font("Arial", Font.BOLD, 18);
        btnCreate.setFont(font);
        btnLoad.setFont(font);
        btnImport.setFont(font); 
        btnManageCats.setFont(font);
        btnExit.setFont(font);

        Dimension buttonSize = new Dimension(0, 60);
        btnCreate.setPreferredSize(buttonSize);
        btnLoad.setPreferredSize(buttonSize);
        btnImport.setPreferredSize(buttonSize); 
        btnManageCats.setPreferredSize(buttonSize);
        btnExit.setPreferredSize(buttonSize);

        btnCreate.addActionListener(e -> cardLayout.show(mainPanel, "Create"));
        
        btnLoad.addActionListener(e -> {
            refreshLoadPage();
            cardLayout.show(mainPanel, "Load");
        });

        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select an Account File (.manager) to Import");
            
            javax.swing.filechooser.FileNameExtensionFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter("Account Manager Files (.manager)", "manager");
            fileChooser.setFileFilter(filter);
            
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    AccountManager importedManager = AccountManager.loadFromFile(selectedFile.getAbsolutePath());
                    
                    if (importedManager != null) {
                        String fileName = selectedFile.getName();
                        String managerName = fileName.replace(".manager", ""); 
                        
                        registry.registerManager(managerName, selectedFile.getAbsolutePath());
                        
                        JOptionPane.showMessageDialog(frame, "Manager '" + managerName + "' imported successfully!");
                        this.manager = importedManager;
                        refreshCurrentView(); 
                        cardLayout.show(mainPanel, "Dashboard");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Error importing file.\nMake sure you selected a valid '.manager' file.\n" + ex.getMessage(),
                        "Import Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnManageCats.addActionListener(e -> openCategoryManager());

        btnExit.addActionListener(e -> System.exit(0));

        painelBotoes.add(btnCreate);
        painelBotoes.add(btnLoad);
        painelBotoes.add(btnImport);
        painelBotoes.add(btnManageCats);
        painelBotoes.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        painelCentral.add(Box.createGlue(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelCentral.add(painelBotoes, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        painelCentral.add(Box.createGlue(), gbc);

        rootPanel.add(painelCentral, BorderLayout.CENTER);

        return rootPanel;
    }

    // --- SCREEN 2: CREATE MANAGER ---
    private JPanel createPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnSettings = new JButton("⚙️");
        btnSettings.setToolTipText("Install/Uninstall Bank Readers");
        btnSettings.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btnSettings.setFocusable(false);
        btnSettings.addActionListener(e -> openSettings());
        painelTopo.add(btnSettings);

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
            
            javax.swing.filechooser.FileNameExtensionFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter("Bank Statements (.xlsx, .csv)", "xlsx", "csv");
            fileChooser.setFileFilter(filter);

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

            String lowerPath = path.toLowerCase(); 
            if (!lowerPath.endsWith(".xlsx") && !lowerPath.endsWith(".csv")) {
                JOptionPane.showMessageDialog(
                    frame, 
                    "Invalid file format!\nPlease select a valid Bank Statement file (.xlsx or .csv).",
                    "Invalid Format",
                    JOptionPane.ERROR_MESSAGE
                );
                return; 
            }

            String selectedBank = showBankSelectionDialog();

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

                    newManager.setBankType(selectedBank);
                    
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

    // --- SCREEN 3: LOAD MANAGER ---
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

    // --- SCREEN 4: DASHBOARD ---
    private JPanel dashboardPanel() {
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
                    frame,
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
            int option = fileChooser.showOpenDialog(frame);

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
                    JOptionPane.showMessageDialog(frame, "Success! Transactions added.");
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                    JOptionPane.showMessageDialog(frame, "Error reading file.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    t.getDate(), 
                    t.getDescription(),
                    typeStr,
                    t.getValue(),
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
        dialog.setSize(750, 500);
        dialog.setResizable(false);
        dialog.setLayout(new GridLayout(1, 2, 10, 0));
        dialog.setLocationRelativeTo(frame);

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

    private void openSettings() {
        JDialog dialog = new JDialog(frame, "Settings - Manage Parsers", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createTitledBorder("Available Bank Readers"));

        SettingsManager settings = SettingsManager.getInstance();
        
        JButton btnSave = new JButton("Save");
        btnSave.setEnabled(false);

        java.util.List<JCheckBox> checkBoxes = new java.util.ArrayList<>();

        java.awt.event.ActionListener changeChecker = e -> {
            boolean hasChanges = false;
            for (JCheckBox cb : checkBoxes) {
                String bankName = cb.getText();
                boolean isSelected = cb.isSelected();
                boolean isInstalled = settings.isInstalled(bankName);

                if (isSelected != isInstalled) {
                    hasChanges = true;
                    break; 
                }
            }
            btnSave.setEnabled(hasChanges);
        };

        for (String bank : SettingsManager.SUPPORTED_BANKS) {
            JCheckBox cb = new JCheckBox(bank);
            
            if (settings.isInstalled(bank)) {
                cb.setSelected(true);
                cb.setForeground(Color.GRAY);
            }
            
            cb.addActionListener(ev -> {
                if (cb.isSelected()) cb.setForeground(Color.GRAY);
                else cb.setForeground(Color.BLACK);
            });

            cb.addActionListener(changeChecker);

            checkBoxes.add(cb);
            listPanel.add(cb);
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            for (JCheckBox cb : checkBoxes) {
                String bankName = cb.getText();
                if (cb.isSelected()) {
                    settings.install(bankName);
                } else {
                    settings.uninstall(bankName);
                }
            }
            settings.save(); 
            JOptionPane.showMessageDialog(dialog, "Settings saved. Readers updated.");
            dialog.dispose();
        });

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnSave);

        dialog.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String showBankSelectionDialog() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        JButton btnSettings = new JButton("⚙️");
        btnSettings.setToolTipText("Install/Uninstall Bank Readers");
        btnSettings.setFocusable(false);
        
        JComboBox<String> comboBanks = new JComboBox<>();

        Runnable refreshCombo = () -> {
            comboBanks.removeAllItems();
            String[] installed = SettingsManager.getInstance().getInstalledBanks();
            for (String s : installed) comboBanks.addItem(s);
        };
        refreshCombo.run();

        btnSettings.addActionListener(e -> {
            openSettings();
            refreshCombo.run();
        });

        topBar.add(btnSettings);

        JPanel centerContent = new JPanel(new GridLayout(2, 1, 0, 5));
        centerContent.add(new JLabel("Select the Bank for this new Manager:"));
        centerContent.add(comboBanks);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerContent, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Create New Manager", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return (String) comboBanks.getSelectedItem();
        }
        return null;
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
                    frame, "Edit Description:", "Edit Transaction", 
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
                frame, "Manually assign category to this specific transaction:", "Set Category Override",
                JOptionPane.QUESTION_MESSAGE, null, cats.toArray(), t.getEffectiveCategory()
            );

            if (selectedCat != null) {
                if (selectedCat.equals(addNewOption)) {
                    String newCatName = JOptionPane.showInputDialog(frame, "Enter name for the new Category:");
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

    // 1. CRIA O BOTÃO VISUAL (O "Falso" ComboBox)
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
            @Override
            public void mousePressed(MouseEvent e) {
                showDateFilterPopup(panel);
            }
        });

        return panel;
    }

    // 2. TEXTO DO BOTÃO
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

    // 3. ATUALIZA O VISUAL (Borda azul quando ativo)
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

    // 4. O NOVO POPUP (Substitui o antigo Dialog)
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

        // --- LÓGICA DE SELEÇÃO (CORRIGIDA PARA ARRASTAR + SHIFT) ---
        
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
            super(new Object()); 
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
            super.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column
            );
            setHorizontalAlignment(JLabel.CENTER);

            if (value instanceof Double) {
                double val = (Double) value;
                setText(String.format("%.2f €", val));
                updateColor(val, isSelected, table);
            }
            else if (value instanceof LocalDate) {
                setText(((LocalDate) value).format(dtf));
                if (!isSelected) setForeground(Color.BLACK);
            }
            else if (value instanceof String) {
                String text = (String) value;
                try {
                    String clean = text
                        .replace("€", "")
                        .replace(",", ".")
                        .trim();
                    double val = Double.parseDouble(clean);
                    updateColor(val, isSelected, table);
                } catch (NumberFormatException e) {
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
                if (val >= 0) setForeground(new Color(0, 150, 0));
                else setForeground(Color.RED); 
            }
        }
    }
}