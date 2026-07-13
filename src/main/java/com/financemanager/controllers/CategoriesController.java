package com.financemanager.controllers;

import com.financemanager.service.CategoryManager;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class CategoriesController {

    @FXML private Button importCategoriesButton = new Button();
    @FXML private Button closeButton = new Button();

    @FXML ListView<String> categoryList;
    @FXML ListView<String> keywordList;

    private final CategoryManager categoryManagerInstance = CategoryManager.getInstance();

    @FXML
    private void handleCloseCategories(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleImportCategories(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Categories File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Data Files (*.dat)", "*.dat");
        fileChooser.getExtensionFilters().addAll(extFilter);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            categoryManagerInstance.importFromFile(file);
            refreshCategoryList();
        }
    }

    private void refreshCategoryList() {
        categoryList.getItems().clear();
        keywordList.getItems().clear();
        categoryList.getItems().addAll(categoryManagerInstance.getOrderedCategories());
    }

    @FXML
    public void initialize() {
        categoryManagerInstance.syncOrder();

        categoryList.getItems().addAll(categoryManagerInstance.getOrderedCategories());

        categoryList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedCat)-> {
            keywordList.getItems().clear();
            if (selectedCat != null) {
                ArrayList<String> keys = categoryManagerInstance.getCategoriesMap().get(selectedCat);
                if (keys != null) {
                    keywordList.getItems().addAll(keys);
                }
            }
        });
        setupCategoryContextMenu();
        setupKeywordContextMenu();
    }

    private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Group");
        dialog.setHeaderText("Create a new category group");
        dialog.showAndWait().ifPresent (name -> {
            String finalName = name.trim();
            if (!finalName.isEmpty()) {
                if (!categoryManagerInstance.getCategoriesMap().containsKey(finalName)) {
                    categoryManagerInstance.addCategory(finalName);
                    categoryList.getItems().add(finalName);
                    categoryManagerInstance.save();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Group Already Exists");
                    alert.showAndWait();
                }
            }
        });
    }

    private void handleEditCategory(String oldName) {
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Rename " + oldName + " to:");
        dialog.showAndWait().ifPresent (newName -> {
            String finalName = newName.trim();
            if (!finalName.isEmpty() && !finalName.equals(oldName)) {
                categoryManagerInstance.renameCategory(oldName, finalName);
                int index = categoryList.getItems().indexOf(oldName);
                categoryList.getItems().set(index, newName);
                categoryManagerInstance.save();
            }
        });
    }

    private void handleRemoveCategory(String name) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete group " + name + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            categoryManagerInstance.removeCategory(name);
            categoryList.getItems().remove(name);
            keywordList.getItems().clear();
            categoryManagerInstance.save();
        }
    }

    // --- MÉTODOS DE KEYWORD (DIREITA) --- [cite: 2026-01-24]

    private void handleAddKeyword() {
        String selectedCat = categoryList.getSelectionModel().getSelectedItem();
        if (selectedCat == null) {
            new Alert(Alert.AlertType.WARNING, "Select a Group on the left first!").showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Keyword");
        dialog.setHeaderText("Add keyword for " + selectedCat);
        dialog.showAndWait().ifPresent (key -> {
            String finalKey = key.trim();
            if (!finalKey.isEmpty()) {
                categoryManagerInstance.addKeyword(selectedCat, finalKey);
                keywordList.getItems().add(finalKey);
                categoryManagerInstance.save();
            }
        });
    }

    private void handleEditKeyword(String oldKey) {
        String selectedCat = categoryList.getSelectionModel().getSelectedItem();
        if (selectedCat == null) {
            return;
        }
        TextInputDialog dialog = new TextInputDialog(oldKey);
        dialog.setTitle("Edit Keyword");
        dialog.setHeaderText("Rename keyword " + oldKey + ":");
        dialog.showAndWait().ifPresent(newKey -> {
            String finalKey = newKey.trim();
            if (!finalKey.isEmpty() && !finalKey.equals(oldKey)) {
                ArrayList<String> keys = categoryManagerInstance.getCategoriesMap().get(selectedCat);
                int index = keys.indexOf(oldKey);
                if (index != -1) {
                    keys.set(index, finalKey);
                    int visualIndex = keywordList.getItems().indexOf(oldKey);
                    keywordList.getItems().set(visualIndex, finalKey);
                    categoryManagerInstance.save();
                }
            }
        });
    }

    private void handleRemoveKeyword(String keyword) {
        String selectedCat = categoryList.getSelectionModel().getSelectedItem();
        if (selectedCat == null) {
            return;
        }
        categoryManagerInstance.getCategoriesMap().get(selectedCat).remove(keyword);
        keywordList.getItems().remove(keyword);
        categoryManagerInstance.save();
    }

    private void setupCategoryContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem add = new MenuItem("Add Category");
        MenuItem edit = new MenuItem("Edit");
        MenuItem remove = new MenuItem("Remove");

        add.setOnAction(e -> handleAddCategory());

        categoryList.setContextMenu(contextMenu);

        categoryList.setOnContextMenuRequested(e -> {
            String selected = categoryList.getSelectionModel().getSelectedItem();
            contextMenu.getItems().clear();
            if (selected != null) {
                edit.setOnAction(ev -> handleEditCategory(selected));
                remove.setOnAction(ev -> handleRemoveCategory(selected));
                contextMenu.getItems().addAll(add, edit, remove);
            } else {
                contextMenu.getItems().add(add);
            }
        });
    }

    private void setupKeywordContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem add = new MenuItem("Add Keyword");
        MenuItem edit = new MenuItem("Edit");
        MenuItem remove = new MenuItem("Remove");

        add.setOnAction(e -> handleAddKeyword());

        keywordList.setContextMenu(contextMenu);

        keywordList.setOnContextMenuRequested(e -> {
            String selected = keywordList.getSelectionModel().getSelectedItem();
            contextMenu.getItems().clear();
            if (selected != null) {
                edit.setOnAction(ev -> handleEditKeyword(selected));
                remove.setOnAction(ev -> handleRemoveKeyword(selected));
                contextMenu.getItems().addAll(add, edit, remove);
            } else {
                contextMenu.getItems().add(add);
            }
        });
    }

}
