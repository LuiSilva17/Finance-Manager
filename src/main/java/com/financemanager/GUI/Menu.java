package com.financemanager.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.financemanager.AccountManager;
import com.financemanager.CSVImporter;
import com.financemanager.Registry;
import com.financemanager.Transaction;

import java.awt.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Menu {

    private JFrame frame;
    private CardLayout cardLayout; // O gestor que troca as telas
    private JPanel mainPanel; // O "baralho" que segura as telas todas
    private Registry registry;
    private AccountManager manager;
    private boolean isEditMode = false;

    private JPanel buttonContainer;
    private JTable transactionsTable;
    private JLabel balanceLabel;
    private JLabel bankNameLabel;

    public Menu() {
        this.registry = new Registry();
        frame = new JFrame();
        frame.setTitle("Finance Manager");
        frame.setSize(800, 500);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void start() {
        addContent(); // Prepara tudo
        frame.setVisible(true);
    }

    public void addContent() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Adicionar TODAS as cartas ao baralho
        mainPanel.add(menuPanel(), "Menu");
        mainPanel.add(createPanel(), "Create");
        mainPanel.add(loadPanel(), "Load");
        mainPanel.add(dashboardPanel(), "Dashboard");

        frame.add(mainPanel);
        cardLayout.show(mainPanel, "Menu");
    }

    // --- TELA 1: MENU PRINCIPAL (ESTILO MINECRAFT / PROPORCIONAL) ---
    private JPanel menuPanel() {
        JPanel painelGeral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Alteração 1: Espaçamento vertical maior (30px) para "respirar"
        JPanel painelBotoes = new JPanel(new GridLayout(3, 1, 0, 30)); 

        JButton btnCreate = new JButton("Create New Manager");
        JButton btnLoad = new JButton("Load Manager");
        JButton btnExit = new JButton("Exit");

        Font font = new Font("Arial", Font.BOLD, 18); // Fonte equilibrada
        btnCreate.setFont(font);
        btnLoad.setFont(font);
        btnExit.setFont(font);
        
        // Alteração 2: Forçar uma altura fixa elegante (60px), mas largura flexível (0)
        // O 0 na largura diz ao Layout: "Ignora a minha largura preferida, usa a do painel pai"
        Dimension buttonSize = new Dimension(0, 60);
        btnCreate.setPreferredSize(buttonSize);
        btnLoad.setPreferredSize(buttonSize);
        btnExit.setPreferredSize(buttonSize);

        btnCreate.addActionListener(e -> cardLayout.show(mainPanel, "Create"));
        btnLoad.addActionListener(e -> {
            refreshLoadPage();
            cardLayout.show(mainPanel, "Load");});
        btnExit.addActionListener(e -> System.exit(0));

        painelBotoes.add(btnCreate);
        painelBotoes.add(btnLoad);
        painelBotoes.add(btnExit);

        // Esquerda (30%)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3; 
        painelGeral.add(Box.createGlue(), gbc);

        // Meio (40% - Muito menos esticado que antes)
        gbc.gridx = 1; 
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        painelGeral.add(painelBotoes, gbc);

        // Direita (30%)
        gbc.gridx = 2; 
        gbc.weightx = 0.3; 
        painelGeral.add(Box.createGlue(), gbc);

        return painelGeral;
    }

    // --- TELA 2: CREATE MANAGER (COM FUNCIONALIDADE CORRIGIDA) ---
    private JPanel createPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        // Topo (Back)
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        painelTopo.add(btnBack);
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        // Centro Proporcional
        JPanel painelProporcional = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // O Formulário
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
            // Logic so that the program gives the original file name, as the manager name
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

        // Montar Form
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
        
        // Esquerda
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        // Meio
        gbc.gridx = 1; 
        gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        painelProporcional.add(form, gbc);

        // Direita
        gbc.gridx = 2; 
        gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
    }

    // --- TELA 3: LOAD MANAGER ---
    private JPanel loadPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        // --- TOP SECTION ---
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Back Button (Left)
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> {
            this.isEditMode = false; // Reset mode when leaving
            cardLayout.show(mainPanel, "Menu");
        });
        
        // Edit Button (Right)
        JButton btnEdit = new JButton("Edit");
        btnEdit.setFont(new Font("Arial", Font.BOLD, 14));
        btnEdit.addActionListener(e -> {
            // Toggle mode
            this.isEditMode = !this.isEditMode;
            
            // Change text based on mode
            if (this.isEditMode) {
                btnEdit.setText("Done");
            } else {
                btnEdit.setText("Edit");
            }
            
            // Redraw list to show/hide red crosses
            refreshLoadPage();
        });

        painelTopo.add(btnBack, BorderLayout.WEST);
        painelTopo.add(btnEdit, BorderLayout.EAST);
        
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        // --- CENTER SECTION ---
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
            // --- EMPTY STATE ---
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
            // --- BANK LIST ---
            for (Map.Entry<String, String> entry : registry.getHashMap().entrySet()) {
                String name = entry.getKey();
                String path = entry.getValue();

                JPanel rowPanel = new JPanel(new BorderLayout(5, 0)); // 5px gap entre botões
                rowPanel.setOpaque(false);

                // Rename button
                if (this.isEditMode) {
                    JButton btnRename = new JButton("✎");
                    btnRename.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
                    btnRename.setBackground(new Color(255, 165, 0)); // Laranja (Cor de edição)
                    btnRename.setForeground(Color.WHITE);
                    btnRename.setFocusPainted(false);
                    btnRename.setPreferredSize(new Dimension(60, 60)); // Quadrado
                    
                    btnRename.addActionListener(e -> {
                        renameManagerFromMenu(name, path);
                    });

                    rowPanel.add(btnRename, BorderLayout.WEST);
                }

                // Main button
                JButton btnManager = new JButton(name);
                btnManager.setFont(new Font("Arial", Font.BOLD, 18));
                btnManager.setPreferredSize(new Dimension(0, 60));
                
                btnManager.addActionListener(e -> {
                    if (this.isEditMode) return; // Bloqueia clique se estiver a editar

                    System.out.println("Loading from: " + path);
                    this.manager = AccountManager.loadFromFile(path);
                    if(this.manager != null) {
                        this.manager.setName(name); 
                        updateDashboardUI();
                        cardLayout.show(mainPanel, "Dashboard");
                    }
                });

                rowPanel.add(btnManager, BorderLayout.CENTER);

                // Delete button
                if (this.isEditMode) {
                    JButton btnDelete = new JButton("X");
                    btnDelete.setFont(new Font("Arial", Font.BOLD, 16));
                    btnDelete.setBackground(Color.RED); // Vermelho (Perigo)
                    btnDelete.setForeground(Color.WHITE);
                    btnDelete.setFocusPainted(false);
                    btnDelete.setPreferredSize(new Dimension(60, 60)); // Quadrado
                    
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

        // --- TOP SECTION ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. LEFT: Back Button
        JButton btnBack = new JButton("Back to Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        topPanel.add(btnBack, BorderLayout.WEST);

        // 2. CENTER: Name + Pencil + Balance
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

        // 3. RIGHT SECTION (View Mode + Add File)
        // Criamos um sub-painel para segurar as duas coisas juntas à direita
        JPanel rightActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // A. View Mode Dropdown (NOVO!)
        String[] modes = {"Default Mode", "Group Mode"};
        JComboBox<String> viewModeBox = new JComboBox<>(modes);
        viewModeBox.setFont(new Font("Arial", Font.BOLD, 14));
        viewModeBox.setFocusable(false); // Tira a borda azul feia de seleção
        viewModeBox.setBackground(Color.WHITE);
        
        // Ação do Dropdown (Preparado para o futuro)
        viewModeBox.addActionListener(e -> {
            String selected = (String) viewModeBox.getSelectedItem();
            if ("Group Mode".equals(selected)) {
                // Futuramente chamaremos aqui a função para agrupar!
                System.out.println("Switching to Group Mode..."); 
                // ex: updateGroupDashboardUI();
            } else {
                System.out.println("Switching to Default Mode...");
                updateDashboardUI(); // Volta à tabela normal
            }
        });

        // B. Add File Button (O teu código anterior)
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

        // Adiciona ambos ao painel da direita
        rightActionPanel.add(viewModeBox);
        rightActionPanel.add(btnAddFile);

        // Adiciona o painel da direita ao topo
        topPanel.add(rightActionPanel, BorderLayout.EAST);

        dashboardPanel.add(topPanel, BorderLayout.NORTH);

        // --- CENTER SECTION (Table) ---
        // Mantém-se igual
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

            // 1. Carregar o manager temporariamente para mudar o nome lá dentro
            AccountManager tempManager = AccountManager.loadFromFile(path);
            
            if (tempManager != null) {
                // 2. Mudar o nome no objeto e gravar no disco
                tempManager.setName(finalName);
                tempManager.saveToFile(); // Isto garante que o ficheiro fica atualizado
                
                // 3. Mudar o nome na Lista Telefónica (Registry)
                registry.renameManager(oldName, finalName);

                // 4. Atualizar a lista visualmente
                refreshLoadPage();
            } else {
                JOptionPane.showMessageDialog(frame, "Error loading file to rename.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }
}