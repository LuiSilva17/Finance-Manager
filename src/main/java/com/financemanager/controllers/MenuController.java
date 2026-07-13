package com.financemanager.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MenuController {

    @FXML private Button create_new_manager_button;
    @FXML private Button load_manager_button;
    @FXML private Button manage_categories_button;
    @FXML private Button exit_button;
    @FXML private Button import_manager_button;
    @FXML private Button settings_button;
    @FXML private StackPane rootPane;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setTitle("Finance Manager - Main Menu");
            stage.centerOnScreen();
        });
    }

    @FXML
    public void handleCreateManager(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CreateManager.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Create Manager");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    @FXML
    public void handleLoadManager(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoadManager.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setTitle("Load Manager");
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    @FXML
    public void handleImportManager(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Existing Manager");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Manager Files (*.manager)", "*.manager"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("Selected File: " + selectedFile.getAbsolutePath());
            // TODO: carregar o manager e navegar para o Dashboard
        } else {
            System.out.println("Selection Cancelled");
        }
    }

    @FXML
    public void handleExit(ActionEvent event) {
        Platform.exit();
        System.exit(0); // exitProcess(0) do Kotlin equivale a System.exit(0) em Java
    }

    @FXML
    public void handleSettings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SettingsView.fxml"));
            Parent root = loader.load();
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(root));
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            settingsStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}