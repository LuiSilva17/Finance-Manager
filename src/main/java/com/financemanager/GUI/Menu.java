package com.financemanager.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.financemanager.AccountManager;
import com.financemanager.CategoryManager; // <--- NOVO IMPORT
import com.financemanager.CSVImporter;
import com.financemanager.Registry;
import com.financemanager.Transaction;

import java.awt.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

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

    public Menu() {
        // --- ALTERAÇÃO 1: Carregar as categorias da memória logo ao abrir o programa ---
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

        // --- ALTERAÇÃO 2: Mudei para 4 linhas para caber o novo botão ---
        JPanel painelBotoes = new JPanel(new GridLayout(4, 1, 0, 20)); // Reduzi ligeiramente o gap vertical para caber tudo

        JButton btnCreate = new JButton("Create New Manager");
        JButton btnLoad = new JButton("Load Manager");
        
        // --- NOVO BOTÃO ---
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
            cardLayout.show(mainPanel, "Load");});
        
        // --- AÇÃO DO NOVO BOTÃO ---
        btnManageCats.addActionListener(e -> openCategoryManager());
        
        btnExit.addActionListener(e -> System.exit(0));

        painelBotoes.add(btnCreate);
        painelBotoes.add(btnLoad);
        painelBotoes.add(btnManageCats); // Adicionar ao painel
        painelBotoes.add(btnExit);

        gbc.gridx = 0; gbc.gridy = 0;
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
            if(option == JFileChooser.APPROVE_OPTION){
               txtPath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton btnCreateManager = new JButton("Create Manager");
        btnCreateManager.setFont(new Font("Arial", Font.BOLD, 18));
        btnCreateManager.setPreferredSize(new Dimension(0, 60));
        btnCreateManager.addActionListener(e -> {
            String path = txtPath.getText();
            if (path.equals("No file selected...")) {
                JOptionPane.showMessageDialog(frame, "Please select a file first.");
                return;
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
            
            AccountManager newManager = CSVImporter.importTransactions(f, trimmedName);
            newManager.setName(trimmedName);
            newManager.saveToFile();
            if (newManager.getFilePath() != null) {
                registry.registerManager(trimmedName, newManager.getFilePath());
                
                this.manager = newManager;
                updateDashboardUI();

                cardLayout.show(mainPanel, "Dashboard");
            }
        });

        gbcForm.gridx = 0; gbcForm.gridy = 0;
        gbcForm.weightx = 0.85; 
        gbcForm.ipady = 15;     
        form.add(txtPath, gbcForm);

        gbcForm.gridx = 1; 
        gbcForm.weightx = 0.15; 
        form.add(btnSearch, gbcForm);

        gbcForm.gridx = 0; gbcForm.gridy = 1;
        gbcForm.gridwidth = 2; 
        gbcForm.weightx = 1.0;
        gbcForm.insets = new Insets(30, 5, 5, 5);
        form.add(btnCreateManager, gbcForm);
        
        gbc.gridx = 0; gbc.gridy = 0;
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
        
        gbc.gridx = 0; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelProporcional.add(this.buttonContainer, gbc);

        gbc.gridx = 2; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
    }

    private void refreshLoadPage() {
        this.buttonContainer.removeAll();

        if (registry.getHashMap().isEmpty()) {
            JLabel emptyMsg = new JLabel("No Manager has been created yet", SwingConstants.CENTER);
            emptyMsg.setFont(new Font("Arial", Font.PLAIN, 16));
            emptyMsg.setForeground(Color.GRAY);
            emptyMsg.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); 

            JButton btnGoToCreate = new JButton("Create Manager");
            btnGoToCreate.setFont(new Font("Arial", Font.BOLD, 18));
            btnGoToCreate.setPreferredSize(new Dimension(0, 60)); 
            btnGoToCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnGoToCreate.addActionListener(e -> cardLayout.show(mainPanel, "Create"));

            this.buttonContainer.add(emptyMsg);
            this.buttonContainer.add(btnGoToCreate);
            this.isEditMode = false;

        } else {
            for (Map.Entry<String, String> entry : registry.getHashMap().entrySet()) {
                String name = entry.getKey();
                String path = entry.getValue();

                JPanel rowPanel = new JPanel(new BorderLayout(5, 0)); 
                rowPanel.setOpaque(false);

                if (this.isEditMode) {
                    JButton btnRename = new JButton("✎");
                    btnRename.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
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
                    if(this.manager != null) {
                        this.manager.setName(name); 
                        updateDashboardUI();
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

        String[] modes = {"Default Mode", "Group Mode"};
        JComboBox<String> viewModeBox = new JComboBox<>(modes);
        viewModeBox.setFont(new Font("Arial", Font.BOLD, 14));
        viewModeBox.setFocusable(false); 
        viewModeBox.setBackground(Color.WHITE);
        
        viewModeBox.addActionListener(e -> {
            String selected = (String) viewModeBox.getSelectedItem();
            if ("Group Mode".equals(selected)) {
                System.out.println("Switching to Group Mode..."); 
            } else {
                System.out.println("Switching to Default Mode...");
                updateDashboardUI(); 
            }
        });

        JButton btnAddFile = new JButton("+ Add File");
        btnAddFile.setFont(new Font("Arial", Font.BOLD, 14));
        btnAddFile.setBackground(new Color(230, 230, 250)); 
        btnAddFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnAddFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(frame);
            
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                AccountManager tempManager = CSVImporter.importTransactions(selectedFile, "Temp");
                
                if (tempManager != null && this.manager != null) {
                    this.manager.merge(tempManager);
                    this.manager.saveToFile();
                    updateDashboardUI(); 
                    JOptionPane.showMessageDialog(frame, "Transactions added successfully!");
                }
            }
        });

        rightActionPanel.add(viewModeBox);
        rightActionPanel.add(btnAddFile);

        topPanel.add(rightActionPanel, BorderLayout.EAST);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Date", "Description", "Type", "Value"};
        Object[][] data = {};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        this.transactionsTable = new JTable(model);
        this.transactionsTable.setRowHeight(30);
        this.transactionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        this.transactionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(this.transactionsTable);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);

        return dashboardPanel;
    }

    private void updateDashboardUI() {
        if (this.manager == null) {
            return;
        }
        
        this.bankNameLabel.setText(this.manager.getName());

        double saldo = this.manager.getCurrentBalance();
        
        balanceLabel.setText(String.format("%.2f €", saldo)); 
        
        if (saldo >= 0) {
            balanceLabel.setForeground(Color.GREEN.darker());
        } else {
            balanceLabel.setForeground(Color.RED);
        }

        DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
        
        model.setRowCount(0); 

        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Transaction t : this.manager.getTransactions()) {
            model.addRow(new Object[]{
                t.getDate().format(formatoData),
                t.getDescription(),
                t.getType(),
                String.format("%.2f €", t.getValue())
            });
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

    // --- ALTERAÇÃO 3: O NOVO MÉTODO DO GESTOR DE CATEGORIAS ---
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
        JButton btnRemCat = new JButton("Remove Category");
        JButton btnInfo = new JButton("ℹ"); 

        leftButtons.add(btnAddCat);
        leftButtons.add(btnRemCat);
        leftButtons.add(btnInfo);

        leftPanel.add(new JScrollPane(categoryList), BorderLayout.CENTER);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        JPanel rightButtons = new JPanel(new FlowLayout());
        JButton btnAddKey = new JButton("Add Keyword");
        JButton btnRemKey = new JButton("Remove Keyword");

        rightButtons.add(btnAddKey);
        rightButtons.add(btnRemKey);

        rightPanel.add(new JScrollPane(keywordList), BorderLayout.CENTER);
        rightPanel.add(rightButtons, BorderLayout.SOUTH);

        // --- LÓGICA (EVENTOS) ---

        // 1. Quando clicas numa Categoria -> Carrega as Keywords na direita
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

        // 2. Botão Adicionar Categoria
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

        // 3. Botão Remover Categoria
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

        // 4. Botão Adicionar Keyword
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

        // 5. Botão Remover Keyword
        btnRemKey.addActionListener(e -> {
            String selectedCat = categoryList.getSelectedValue();
            String selectedKey = keywordList.getSelectedValue();
            
            if (selectedCat != null && selectedKey != null) {
                catManager.getCategoriesMap().get(selectedCat).remove(selectedKey);
                keywordModel.removeElement(selectedKey); 
                catManager.save();
            }
        });

        // 6. Botão de Informação
        btnInfo.addActionListener(e -> {
            String message = "How to use Keywords:\n\n" +
                             "1. Create a Group (e.g., 'Supermarket').\n" +
                             "2. Select the group.\n" +
                             "3. Add unique words found in your bank statement description.\n" +
                             "   Example: If the statement says 'VISA PINGO DOCE 234', add 'PINGO DOCE'.\n\n" +
                             "The program will automatically link transactions containing these words to the group.";
            JOptionPane.showMessageDialog(dialog, message, "Help", JOptionPane.INFORMATION_MESSAGE);
        });

        dialog.add(leftPanel);
        dialog.add(rightPanel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }
}