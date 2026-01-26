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
    private JPanel painelPrincipal; // O "baralho" que segura as telas todas
    private Registry registry;
    private AccountManager manager;

    private JPanel buttonContainer;

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
        painelPrincipal = new JPanel(cardLayout);

        // Adicionar TODAS as cartas ao baralho
        painelPrincipal.add(menuPanel(), "Menu");
        painelPrincipal.add(createPanel(), "Create");
        painelPrincipal.add(loadPanel(), "Load");
        painelPrincipal.add(dashboardPanel(), "Dashboard");

        frame.add(painelPrincipal);
        cardLayout.show(painelPrincipal, "Menu");
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

        btnCreate.addActionListener(e -> cardLayout.show(painelPrincipal, "Create"));
        btnLoad.addActionListener(e -> {
            refreshLoadPage();
            cardLayout.show(painelPrincipal, "Load");});
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
        btnBack.addActionListener(e -> cardLayout.show(painelPrincipal, "Menu"));
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
            registry.registerManager(trimmedName, path);
            
            cardLayout.show(painelPrincipal, "Dashboard");
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

        // 1. O Topo (Back Button) - IGUAL
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(painelPrincipal, "Menu"));
        painelTopo.add(btnBack);
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        // 2. O Centro (Layout Proporcional) - IGUAL
        JPanel painelProporcional = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 3. A Lista de Bancos (AQUI MUDA)
        // Inicializamos a variável global, mas deixamo-la VAZIA por agora
        this.buttonContainer = new JPanel(new GridLayout(0, 1, 0, 15)); 
        
        // Configuração do Layout - IGUAL
        gbc.gridx = 0; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        painelProporcional.add(this.buttonContainer, gbc); // Adicionamos a variável global

        gbc.gridx = 2; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
    }

    private void refreshLoadPage() {
        this.buttonContainer.removeAll();
        for (Map.Entry<String, String> entry : registry.getHashMap().entrySet()) {
            JButton button = new JButton(entry.getKey());
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setPreferredSize(new Dimension(0, 60));
            
            button.addActionListener(e -> {
                String path = entry.getValue();
                System.out.println("Loading from: " + path);
                this.manager = AccountManager.loadFromFile(path);
                if(this.manager != null) {
                    updateDashboardUI();
                    cardLayout.show(painelPrincipal, "Dashboard");
                }
            });
            buttonContainer.add(button);
        }
        this.buttonContainer.revalidate();
        this.buttonContainer.repaint();
    }

    // --- TELA 4: DASHBOARD ---
    private JPanel dashboardPanel() {
        JPanel painelGeral = new JPanel(new BorderLayout());

        // Botão Back
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back to Menu");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(painelPrincipal, "Menu"));
        
        painelTopo.add(btnBack);
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        // Conteúdo Temporário
        JLabel lblTitulo = new JLabel("DASHBOARD", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 40));
        lblTitulo.setForeground(Color.DARK_GRAY);
        
        painelGeral.add(lblTitulo, BorderLayout.CENTER);

        return painelGeral;
    }

    private void updateDashboardUI() {
        // Segurança: Se não houver manager carregado, não faz nada
        if (this.manager == null) return;

        // 1. Atualizar Títulos e Saldos
        // lblTitle.setText("Banco: " + this.manager.getName()); 
        
        double saldo = this.manager.getCurrentBalance();
        
        // Formatar dinheiro bonito (ex: 1.250,50 €)
        lblSaldo.setText(String.format("%.2f €", saldo)); 
        
        // Mudar a cor do saldo (Verde se positivo, Vermelho se negativo)
        if (saldo >= 0) {
            lblSaldo.setForeground(Color.GREEN.darker()); // Verde escuro para ler melhor
        } else {
            lblSaldo.setForeground(Color.RED);
        }

        // 2. Preencher a Tabela
        // Primeiro, obtemos o "Modelo" da tabela para poder mexer nos dados
        DefaultTableModel model = (DefaultTableModel) tabelaTransacoes.getModel();
        
        // Limpar a tabela antiga (se não fizeres isto, os dados acumulam-se!)
        model.setRowCount(0); 

        // Loop pelas transações
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Transaction t : this.manager.getTransactions()) {
            model.addRow(new Object[]{
                t.getDate().format(formatoData), // Data formatada
                t.getDescription(),              // Descrição
                t.getType(),                     // O teu Enum (Crédito/Débito)
                String.format("%.2f €", t.getValue()) // Valor
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }
}