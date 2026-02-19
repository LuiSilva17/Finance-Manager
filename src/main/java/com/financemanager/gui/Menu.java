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
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Menu {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Registry registry;
    private AccountManager manager;
    private boolean isEditMode = false;

    private JPanel buttonContainer;

    private DashboardPanel dashBoardPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }

    public Menu() {
        CategoryManager.getInstance().load();
        SettingsManager.getInstance().load();

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
        this.dashBoardPanel = new DashboardPanel(this.cardLayout, this.mainPanel);

        mainPanel.add(menuPanel(), "Menu");
        mainPanel.add(createPanel(), "Create");
        mainPanel.add(loadPanel(), "Load");
        mainPanel.add(this.dashBoardPanel, "Dashboard");

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
                        this.dashBoardPanel.setManager(this.manager);
                        this.dashBoardPanel.refreshCurrentView(); 
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

        btnManageCats.addActionListener(e -> new CategoryDialog(frame).setVisible(true));

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
                        this.dashBoardPanel.setManager(this.manager);
                        this.dashBoardPanel.refreshCurrentView();
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
                        this.dashBoardPanel.refreshCurrentView();
                        this.dashBoardPanel.setManager(this.manager);
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
}