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
import javafx.stage.Modality

import java.io.IOException
import kotlin.system.exitProcess


class MenuController {

    @FXML var create_new_manager_button: Button = Button()
    @FXML var load_manager_button: Button = Button()
    @FXML var manage_categories_button: Button = Button()
    @FXML var exit_button: Button = Button()
    @FXML var import_manager_button: Button = Button()
    @FXML var settings_button: Button = Button()

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
        try {
            val loader = FXMLLoader(javaClass.getResource("/LoadManager.fxml"))
            val settingsRoot = loader.load<Parent>()

            val loadManagerScene = Scene(settingsRoot)

            val currentStage = (event.source as Node).scene.window as Stage

            currentStage.apply {
                scene = loadManagerScene
                title = "Load Manager"
                show()
            }
        } catch (e: IOException) {
            println("Error loading FXML: ${e.message}")
        }
    }

    @FXML
    fun handleImportManager(event: ActionEvent) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/LoadManager.fxml"))
            val settingsRoot = loader.load<Parent>()

            val loadManagerScene = Scene(settingsRoot)

            val currentStage = (event.source as Node).scene.window as Stage

            currentStage.apply {
                scene = loadManagerScene
                title = "Load Manager"
                show()
            }
        } catch (e: IOException) {
            println("Error loading FXML: ${e.message}")
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

}