package com.financemanager.GUI;

import javax.swing.*;

import com.financemanager.AccountManager;
import com.financemanager.CSVImporter;
import com.financemanager.Registry;

import java.awt.*;
import java.io.File;

public class Menu {

    private JFrame frame;
    private CardLayout cardLayout; // O gestor que troca as telas
    private JPanel painelPrincipal; // O "baralho" que segura as telas todas
    private Registry registry;

    public Menu() {
        this.registry = new Registry("finance_config.dat");
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
        btnLoad.addActionListener(e -> cardLayout.show(painelPrincipal, "Load"));
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
            
            AccountManager newManager = CSVImporter.importTransactions(f, path);
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

        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.addActionListener(e -> cardLayout.show(painelPrincipal, "Menu"));
        painelTopo.add(btnBack);
        painelGeral.add(painelTopo, BorderLayout.NORTH);

        JPanel painelProporcional = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // A Lista de Bancos
        JPanel listaBancos = new JPanel(new GridLayout(0, 1, 0, 15)); 

        // --- SIMULAÇÃO: Imagina que o código leu o config e encontrou estes 3 ---
        String[] meusBancos = {"Crédito Agrícola", "Caixa Geral de Depósitos", "BPI"};

        for (String nomeBanco : meusBancos) {
            JButton btnBanco = new JButton(nomeBanco);
            btnBanco.setFont(new Font("Arial", Font.BOLD, 18));
            btnBanco.setPreferredSize(new Dimension(0, 60)); // Altura elegante
            
            btnBanco.addActionListener(e -> {
                System.out.println("A carregar: " + nomeBanco); // Só para debug
                cardLayout.show(painelPrincipal, "Dashboard");
            });
            
            listaBancos.add(btnBanco);
        }

        gbc.gridx = 0; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        gbc.gridx = 1; gbc.weightx = 0.4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Dica: Se tiveres muitos bancos, aqui teremos de meter um JScrollPane no futuro.
        // Para já, como são poucos, o painel simples serve.
        painelProporcional.add(listaBancos, gbc);

        gbc.gridx = 2; gbc.weightx = 0.3;
        painelProporcional.add(Box.createGlue(), gbc);

        painelGeral.add(painelProporcional, BorderLayout.CENTER);
        return painelGeral;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.start();
        });
    }
}