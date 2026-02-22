package com.financemanager.controllers

import com.financemanager.service.CategoryManager
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog

class CategoriesController {

    @FXML var importCategoriesButton: Button = Button()
    @FXML var closeButton: Button = Button()

    @FXML lateinit var categoryList: ListView<String>
    @FXML lateinit var keywordList: ListView<String>

    @FXML
    fun handleCloseCategories(event: ActionEvent) {
        val stage = (event.source as Button).scene.window as javafx.stage.Stage
        stage.close()
    }

    @FXML
    fun handleImportCategories(event: ActionEvent) {
        val fileChooser = javafx.stage.FileChooser()
        fileChooser.title = "Import Categories File"
        val extFilter = javafx.stage.FileChooser.ExtensionFilter("Data Files (*.dat)", "*.dat")
        fileChooser.extensionFilters.add(extFilter)

        val stage = (event.source as javafx.scene.Node).scene.window as javafx.stage.Stage
        val file = fileChooser.showOpenDialog(stage)

        if (file != null) {
            CategoryManager.getInstance().importCategories(file)
            refreshCategoryList()
            println("Ficheiro importado com sucesso: ${file.name}")
        }
    }

    private fun refreshCategoryList() {
        categoryList.items.clear()
        keywordList.items.clear()
        categoryList.items.addAll(CategoryManager.getInstance().orderedCategories)
    }

    @FXML
    fun initialize() {
        println("DEBUG: CategoriesController initialized!")
        val catManager = CategoryManager.getInstance()
        catManager.syncOrder()

        categoryList.items.addAll(catManager.orderedCategories)

        // Sincronização das listas [cite: 2026-01-24]
        categoryList.selectionModel.selectedItemProperty().addListener { _, _, selectedCat ->
            keywordList.items.clear()
            if (selectedCat != null) {
                val keys = catManager.categoriesMap[selectedCat]
                if (keys != null) {
                    keywordList.items.addAll(keys)
                }
            }
        }

        // Configuração dos menus de contexto (Botão Direito) [cite: 2026-01-24]
        setupCategoryContextMenu()
        setupKeywordContextMenu()
    }

    // --- MÉTODOS DE CATEGORIA (ESQUERDA) --- [cite: 2026-01-24]

    private fun handleAddCategory() {
        val dialog = TextInputDialog()
        dialog.title = "New Group"
        dialog.headerText = "Create a new category group"
        dialog.showAndWait().ifPresent { name ->
            val finalName = name.trim()
            if (finalName.isNotEmpty()) {
                if (!CategoryManager.getInstance().categoriesMap.containsKey(finalName)) {
                    CategoryManager.getInstance().addCategory(finalName)
                    categoryList.items.add(finalName)
                    CategoryManager.getInstance().save()
                } else {
                    Alert(Alert.AlertType.ERROR, "Group already exists!").showAndWait()
                }
            }
        }
    }

    private fun handleEditCategory(oldName: String) {
        val dialog = TextInputDialog(oldName)
        dialog.title = "Edit Category"
        dialog.headerText = "Rename '$oldName' to:"
        dialog.showAndWait().ifPresent { newName ->
            val finalName = newName.trim()
            if (finalName.isNotEmpty() && finalName != oldName) {
                CategoryManager.getInstance().renameCategory(oldName, finalName)
                val index = categoryList.items.indexOf(oldName)
                categoryList.items[index] = finalName
                CategoryManager.getInstance().save()
            }
        }
    }

    private fun handleRemoveCategory(name: String) {
        val alert = Alert(Alert.AlertType.CONFIRMATION, "Delete group '$name'?", ButtonType.YES, ButtonType.NO)
        if (alert.showAndWait().get() == ButtonType.YES) {
            CategoryManager.getInstance().removeCategory(name)
            categoryList.items.remove(name)
            keywordList.items.clear()
            CategoryManager.getInstance().save()
        }
    }

    // --- MÉTODOS DE KEYWORD (DIREITA) --- [cite: 2026-01-24]

    private fun handleAddKeyword() {
        val selectedCat = categoryList.selectionModel.selectedItem
        if (selectedCat == null) {
            Alert(Alert.AlertType.WARNING, "Select a Group on the left first!").showAndWait()
            return
        }
        val dialog = TextInputDialog()
        dialog.title = "Add Keyword"
        dialog.headerText = "Add keyword for $selectedCat"
        dialog.showAndWait().ifPresent { key ->
            val finalKey = key.trim()
            if (finalKey.isNotEmpty()) {
                CategoryManager.getInstance().addKeyword(selectedCat, finalKey)
                keywordList.items.add(finalKey)
                CategoryManager.getInstance().save()
            }
        }
    }

    private fun handleEditKeyword(oldKey: String) {
        val selectedCat = categoryList.selectionModel.selectedItem ?: return
        val dialog = TextInputDialog(oldKey)
        dialog.title = "Edit Keyword"
        dialog.headerText = "Rename keyword '$oldKey':"
        dialog.showAndWait().ifPresent { newKey ->
            val finalKey = newKey.trim()
            if (finalKey.isNotEmpty() && finalKey != oldKey) {
                val keys = CategoryManager.getInstance().categoriesMap[selectedCat]
                val index = keys?.indexOf(oldKey) ?: -1
                if (index != -1) {
                    keys?.set(index, finalKey)
                    val visualIndex = keywordList.items.indexOf(oldKey)
                    keywordList.items[visualIndex] = finalKey
                    CategoryManager.getInstance().save()
                }
            }
        }
    }

    private fun handleRemoveKeyword(keyword: String) {
        val selectedCat = categoryList.selectionModel.selectedItem ?: return
        CategoryManager.getInstance().categoriesMap[selectedCat]?.remove(keyword)
        keywordList.items.remove(keyword)
        CategoryManager.getInstance().save()
    }

    // --- CONFIGURAÇÃO DOS MENUS DE CONTEXTO --- [cite: 2026-01-24]

    private fun setupCategoryContextMenu() {
        val contextMenu = ContextMenu()
        val add = MenuItem("Add Category").apply { setOnAction { handleAddCategory() } }
        val edit = MenuItem("Edit")
        val remove = MenuItem("Remove")

        categoryList.contextMenu = contextMenu

        // Antes de mostrar o menu, verificamos se há seleção [cite: 2026-01-24]
        categoryList.setOnContextMenuRequested {
            val selected = categoryList.selectionModel.selectedItem
            contextMenu.items.clear()
            if (selected != null) {
                edit.setOnAction { handleEditCategory(selected) }
                remove.setOnAction { handleRemoveCategory(selected) }
                contextMenu.items.addAll(add, edit, remove)
            } else {
                contextMenu.items.add(add)
            }
        }
    }

    private fun setupKeywordContextMenu() {
        val contextMenu = ContextMenu()
        val add = MenuItem("Add Keyword").apply { setOnAction { handleAddKeyword() } }
        val edit = MenuItem("Edit")
        val remove = MenuItem("Remove")

        keywordList.contextMenu = contextMenu

        keywordList.setOnContextMenuRequested {
            val selected = keywordList.selectionModel.selectedItem
            contextMenu.items.clear()
            if (selected != null) {
                edit.setOnAction { handleEditKeyword(selected) }
                remove.setOnAction { handleRemoveKeyword(selected) }
                contextMenu.items.addAll(add, edit, remove)
            } else {
                contextMenu.items.add(add)
            }
        }
    }
}