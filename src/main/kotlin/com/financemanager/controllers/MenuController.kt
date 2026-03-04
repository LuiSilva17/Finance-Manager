package com.financemanager.controllers

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.Node
import javafx.stage.Stage
import javafx.event.ActionEvent
import javafx.scene.control.Button
import javafx.application.Platform
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import javafx.stage.Modality
import java.io.File

import java.io.IOException
import kotlin.system.exitProcess


class MenuController {

    @FXML var create_new_manager_button: Button = Button()
    @FXML var load_manager_button: Button = Button()
    @FXML var manage_categories_button: Button = Button()
    @FXML var exit_button: Button = Button()
    @FXML var import_manager_button: Button = Button()
    @FXML var settings_button: Button = Button()
    @FXML var rootPane: StackPane = StackPane()

    @FXML
    fun initialize() {
        javafx.application.Platform.runLater {
            val stage = rootPane.scene.window as javafx.stage.Stage
            stage.isResizable = false
            stage.sizeToScene()
            stage.title = "Finance Manager - Main Menu"
            stage.centerOnScreen()
        }
    }

    @FXML
    fun handleCreateManager(event: ActionEvent) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/CreateManager.fxml"))
            val settingsRoot = loader.load<Parent>()

            val menuScene = Scene(settingsRoot)

            val currentStage = (event.source as Node).scene.window as Stage

            currentStage.apply {
                scene = menuScene
                title = "Create Manager"
                show()
            }
        } catch (e: IOException) {
            println("Error loading FXML: ${e.message}")
        }
    }

    @FXML
    fun handleLoadManager(event: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("/LoadManager.fxml"))
        val root = loader.load<Parent>()
        val stage = (event.source as Node).scene.window as Stage
        stage.scene = Scene(root)
        stage.isResizable = false
        stage.sizeToScene()
        stage.centerOnScreen()
        stage.title = "Load Manager"
    }

    @FXML
    fun handleImportManager(event: ActionEvent) {
        val fileChooser = FileChooser()
        fileChooser.title = "Import Existing Manager"

        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Manager Files (*.manager)", "*.manager"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        val currentStage = (event.source as Node).scene.window as Stage

        val selectedFile: File? = fileChooser.showOpenDialog(currentStage)

        if (selectedFile != null) {
            println("Selected File: ${selectedFile.absolutePath}")
        } else {
            println("Selection Cancelled")
        }
    }

    @FXML
    fun handleExit(event: ActionEvent) {
        Platform.exit()
        exitProcess(0)
    }

    @FXML
    fun handleSettings(event: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("/SettingsView.fxml"))
        val root = loader.load<Parent>()

        val settingsStage = Stage().apply {
            title = "Settings"
            scene = Scene(root)

            initModality(Modality.APPLICATION_MODAL)
            initOwner((event.source as Node).scene.window)
        }
        settingsStage.showAndWait()
    }

    @FXML
    fun handleCategories(event: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("/CategoriesView.fxml"))
        val root = loader.load<Parent>()

        val categoriesStage = Stage().apply {
            title = "Categories"
            scene = Scene(root)

            initModality(Modality.APPLICATION_MODAL)
            initOwner((event.source as Node).scene.window)
        }
        categoriesStage.showAndWait()
    }

}