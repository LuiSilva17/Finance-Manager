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

    private JComboBox<String> viewModeBox;

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

    /*private void openCategoryManager() {
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
    }*/

    /*private void refreshCurrentView() {
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
    }*/

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