package com.financemanager.controllers;

import com.financemanager.IO.importers.CGDParser;
import com.financemanager.IO.importers.CreditoAgricolaParser;
import com.financemanager.model.Transaction;
import com.financemanager.model.TransactionEnum;
import com.financemanager.service.AccountManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    private AccountManager manager;

    @FXML private Label bankNameLabel;
    @FXML private Label balanceLabel;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, TransactionEnum> typeColumn;
    @FXML private TableColumn<Transaction, BigDecimal> valueColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    @FXML private Button addFileButton;
    @FXML private Button categoriesButton;
    @FXML private Button backToMenuButton;

    public void initData(AccountManager manager) {
        this.manager = manager;
        setupTable();
        refreshUI();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Usa getEffectiveCategory() para respeitar categorias manuais e automáticas
        categoryColumn.setCellValueFactory(cellData -> {
            String category = cellData.getValue().getEffectiveCategory();
            return new SimpleStringProperty(category);
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Formata a data para dd/MM/yyyy na tabela
        // Em Java, anonymous class em vez do 'object :' do Kotlin
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });

        // Ordenação por data definida uma vez aqui, não no refreshUI
        transactionTable.getSortOrder().setAll(dateColumn);
    }

    private void refreshUI() {
        bankNameLabel.setText(manager.getName());
        BigDecimal balance = manager.getCurrentBalance();
        balanceLabel.setText(String.format("%.2f €", balance));

        if (balance.signum() >= 0) {
            balanceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        } else {
            balanceLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        transactionTable.setItems(FXCollections.observableArrayList(manager.getTransactions()));
    }

    @FXML
    public void handleCategories(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategoriesView.fxml"));
            Parent root = loader.load();
            Stage categoriesStage = new Stage();
            categoriesStage.setTitle("Categories");
            categoriesStage.setScene(new Scene(root));
            categoriesStage.initModality(Modality.APPLICATION_MODAL);
            categoriesStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            categoriesStage.showAndWait();
            refreshUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddFile(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 1. Garante que o tipo de banco está definido
        if (manager.getBankType() == null) {
            List<String> banks = List.of("Crédito Agrícola", "CGD");
            ChoiceDialog<String> dialog = new ChoiceDialog<>(banks.get(0), banks);
            dialog.setTitle("Select Bank Type");
            dialog.setHeaderText("Identify the bank statement format.");
            dialog.setContentText("Bank:");

            var result = dialog.showAndWait();
            if (result.isPresent()) {
                manager.setBankType(result.get());
                manager.saveToFile();
            } else {
                return;
            }
        }

        // 2. Seleção de ficheiro
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Bank Statement");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Statement Files", "*.csv", "*.xlsx", "*.xls")
        );

        java.io.File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                // 3. Parser e Merge
                var parser = manager.getBankType().equals("CGD") ? new CGDParser() : new CreditoAgricolaParser();
                List<Transaction> newTransactions = parser.parse(file);
                manager.mergeTransactions(newTransactions);
                manager.saveToFile();
                refreshUI();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to import: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    public void handleBackToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setTitle("Finance Manager - Menu");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}