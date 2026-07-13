package com.financemanager.controllers;

import com.financemanager.IO.importers.BankStatementParser;
import com.financemanager.IO.importers.CGDParser;
import com.financemanager.IO.importers.CreditoAgricolaParser;
import com.financemanager.model.Transaction;
import com.financemanager.service.AccountManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CreateManagerController {

    @FXML private Button back_to_menu_button;
    @FXML private Button import_file_path_button;
    @FXML private Button import_file_button;
    @FXML private Label file_name_label;

    private String filePath = "";

    @FXML
    public void handleBacktoMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    @FXML
    public void handleImportMenu(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import New Manager");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV/Excel Files", "*.csv", "*.xlsx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile != null) {
            filePath = selectedFile.getAbsolutePath(); // só aqui, depois do null check
            file_name_label.setText(selectedFile.getName());
            System.out.println("Selected File: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("Selection Cancelled");
        }
    }

    @FXML
    public void handleImportManager(ActionEvent event) {
        if (filePath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Selection Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a valid file before proceeding.");
            alert.showAndWait();
            return;
        }

        String selectedBank = showBankSelectionDialog();
        if (selectedBank == null) return; // utilizador cancelou

        BankStatementParser parser = switch (selectedBank) {
            case "Crédito Agrícola" -> new CreditoAgricolaParser();
            case "CGD" -> new CGDParser();
            default -> new CreditoAgricolaParser();
        };

        File file = new File(filePath);
        // Kotlin tinha file.nameWithoutExtension — em Java faz-se assim:
        String trimmedName = file.getName().replaceFirst("[.][^.]+$", "");

        try {
            List<Transaction> transactions = parser.parse(file);
            AccountManager newManager = new AccountManager(trimmedName);
            newManager.setBankType(selectedBank);
            newManager.loadTransactions(transactions);
            newManager.saveToFile();
            navigateToDashboard(event, newManager);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private String showBankSelectionDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Manager");

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        ComboBox<String> comboBanks = new ComboBox<>();
        comboBanks.setItems(FXCollections.observableArrayList("Crédito Agrícola", "CGD"));
        comboBanks.getSelectionModel().selectFirst();
        comboBanks.setMaxWidth(Double.MAX_VALUE);

        Button btnSettings = new Button("⚙️");
        btnSettings.setOnAction(e -> openSettings());
        // quando o SettingsManager estiver feito, atualiza a lista de bancos aqui

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
                new HBox(btnSettings),
                new Label("Select the Bank for this new Manager:"),
                comboBanks
        );

        dialog.getDialogPane().setContent(layout);

        // resultConverter: diz ao Dialog o que devolver quando o utilizador carrega OK ou CANCEL
        dialog.setResultConverter(buttonType -> {
            if (buttonType == okButtonType) return comboBanks.getValue();
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SettingsView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Bank Reader Settings");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println("Error opening Settings: " + e.getMessage());
        }
    }

    private void navigateToDashboard(ActionEvent event, AccountManager manager) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            // getController() busca o controller já instanciado pelo FXMLLoader
            DashboardController controller = loader.getController();
            controller.initData(manager);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}