package com.financemanager.controllers;

import com.financemanager.service.AccountManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class LoadManagerController {

    @FXML private Button back_to_menu_button;
    @FXML private Button editButton;
    @FXML private VBox emptyStateView;
    @FXML private VBox managerListView;

    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Listener encadeado: espera que o botão tenha Scene, e a Scene tenha Stage
        // Necessário porque no initialize() o Stage ainda não está disponível
        back_to_menu_button.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWin, newWin) -> {
                    if (newWin instanceof Stage stage) { // pattern matching - Java 16+
                        stage.setResizable(false);
                        stage.sizeToScene();
                        stage.centerOnScreen();
                    }
                });
            }
        });
        refreshManagerList();
    }

    @FXML
    public void handleToggleEdit(ActionEvent event) {
        isEditMode = !isEditMode;
        editButton.setText(isEditMode ? "Done" : "Edit");
        refreshManagerList();
    }

    private void refreshManagerList() {
        String path = System.getProperty("user.home") + File.separator + "Finance Manager Data" + File.separator + "Managers";
        File folder = new File(path);
        if (!folder.exists()) folder.mkdirs();

        // Lambda como filtro — equivalente ao { _, name -> name.endsWith(".manager") } do Kotlin
        File[] managerFiles = folder.listFiles((dir, name) -> name.endsWith(".manager"));

        if (managerFiles != null && managerFiles.length > 0) {
            toggleViews(true);
            managerListView.getChildren().clear();
            managerListView.setSpacing(15);

            for (File file : managerFiles) {
                String managerName = file.getName().replaceFirst("[.][^.]+$", ""); // nameWithoutExtension

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER);

                // --- BOTÃO RENAME (só em Edit Mode) ---
                if (isEditMode) {
                    Button btnRename = new Button("✎");
                    btnRename.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold;");
                    btnRename.setPrefWidth(45);
                    btnRename.setPrefHeight(45);
                    btnRename.setOnAction(e -> handleRename(file));
                    row.getChildren().add(btnRename);
                }

                // --- BOTÃO DO MANAGER ---
                Button btn = new Button(managerName);
                btn.setPrefWidth(isEditMode ? 200 : 300);
                btn.setPrefHeight(45);
                btn.setDisable(isEditMode);
                btn.setOnAction(e -> {
                    AccountManager am = new AccountManager(managerName);
                    AccountManager loaded = am.loadFromFile(file);
                    openDashboard(loaded);
                });
                row.getChildren().add(btn);

                // --- BOTÃO DELETE (só em Edit Mode) ---
                if (isEditMode) {
                    Button btnDelete = new Button("X");
                    btnDelete.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
                    btnDelete.setPrefWidth(45);
                    btnDelete.setPrefHeight(45);
                    btnDelete.setOnAction(e -> handleDelete(file));
                    row.getChildren().add(btnDelete);
                }

                managerListView.getChildren().add(row);
            }
        } else {
            toggleViews(false);
            isEditMode = false;
            editButton.setText("Edit");
        }
    }

    private void handleRename(File file) {
        String nameWithoutExt = file.getName().replaceFirst("[.][^.]+$", "");
        TextInputDialog dialog = new TextInputDialog(nameWithoutExt);
        dialog.setTitle("Rename Manager");
        dialog.setHeaderText("Renaming '" + nameWithoutExt + "'");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isBlank()) {
                File newFile = new File(file.getParent(), newName + ".manager");
                if (file.renameTo(newFile)) {
                    AccountManager am = new AccountManager(newName);
                    AccountManager loaded = am.loadFromFile(newFile);
                    loaded.setName(newName);
                    loaded.saveToFile();
                    refreshManagerList();
                }
            }
        });
    }

    private void handleDelete(File file) {
        String nameWithoutExt = file.getName().replaceFirst("[.][^.]+$", "");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Manager");
        alert.setHeaderText("Confirm deletion of '" + nameWithoutExt + "'");
        alert.setContentText("This will permanently remove the manager file from your computer.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (file.delete()) refreshManagerList();
        }
    }

    private void toggleViews(boolean showList) {
        managerListView.setVisible(showList);
        managerListView.setManaged(showList);
        emptyStateView.setVisible(!showList);
        emptyStateView.setManaged(!showList);
    }

    @FXML
    public void handleImport(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import .manager File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Manager Files", "*.manager")
        );

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            String name = file.getName().replaceFirst("[.][^.]+$", "");
            AccountManager am = new AccountManager(name);
            AccountManager imported = am.loadFromFile(file);
            openDashboard(imported);
        }
    }

    @FXML
    public void handleCreate(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/CreateManager.fxml"));
            navigateTo(root, "Create New Manager", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDashboard(AccountManager manager) {
        if (manager == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.initData(manager);
            navigateTo(root, "Dashboard", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBacktoMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Menu.fxml"));
            navigateTo(root, "Menu", false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(Parent root, String title, boolean resizable) {
        Stage stage = (Stage) back_to_menu_button.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.setResizable(resizable);
        if (!resizable) stage.sizeToScene();
        stage.centerOnScreen();
    }
}