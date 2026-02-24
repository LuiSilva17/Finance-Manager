package com.financemanager.controllers

import com.financemanager.service.AccountManager
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.io.IOException

class LoadManagerController {

    @FXML lateinit var back_to_menu_button: Button
    @FXML lateinit var emptyStateView: VBox
    @FXML lateinit var managerListView: VBox

    @FXML
    fun initialize() {
        back_to_menu_button.sceneProperty().addListener { _, oldScene, newScene ->
            if (newScene != null) {
                newScene.windowProperty().addListener { _, oldWin, newWin ->
                    if (newWin != null) {
                        val stage = newWin as Stage
                        stage.isResizable = false
                        stage.sizeToScene()
                        stage.centerOnScreen()
                    }
                }
            }
        }
        refreshManagerList()
    }

    private fun refreshManagerList() {
        val path = System.getProperty("user.home") + File.separator + "Finance Manager Data"
        val folder = File(path)
        val managerFiles = folder.listFiles { _, name -> name.endsWith(".manager") }

        if (managerFiles != null && managerFiles.isNotEmpty()) {
            toggleViews(true)
            managerListView.children.clear()

            managerFiles.forEach { file ->
                val managerName = file.nameWithoutExtension

                val btn = Button(managerName).apply {
                    prefWidth = 250.0
                    setOnAction {
                        val am = AccountManager(managerName)
                        val loadedManager = am.loadFromFile(file)
                        openDashboard(loadedManager)
                    }
                }
                managerListView.children.add(btn)
            }
        } else {
            toggleViews(false)
        }
    }

    private fun toggleViews(showList: Boolean) {
        managerListView.isVisible = showList
        managerListView.isManaged = showList
        emptyStateView.isVisible = !showList
        emptyStateView.isManaged = !showList
    }

    @FXML
    fun handleImport(event: ActionEvent) {
        val fileChooser = FileChooser().apply {
            title = "Import .manager File"
            extensionFilters.add(FileChooser.ExtensionFilter("Manager Files", "*.manager"))
        }
        val file = fileChooser.showOpenDialog((event.source as Node).scene.window)

        if (file != null) {
            val am = AccountManager(file.nameWithoutExtension)
            val imported = am.loadFromFile(file)
            openDashboard(imported)
        }
    }

    @FXML
    fun handleCreate(event: ActionEvent) {
        navigateTo("/CreateManager.fxml", "Create New Manager")
    }

    private fun openDashboard(manager: AccountManager?) {
        navigateTo("/Dashboard.fxml", "Dashboard")
    }

    @FXML
    fun handleBacktoMenu(event: ActionEvent) {
        navigateTo("/Menu.fxml", "Menu")
    }

    private fun navigateTo(fxmlPath: String, titleStr: String) {
        try {
            val loader = FXMLLoader(javaClass.getResource(fxmlPath))
            val root = loader.load<Parent>()
            val stage = back_to_menu_button.scene.window as Stage
            stage.scene = Scene(root)
            stage.title = titleStr
            if (titleStr == "Menu") {
                stage.isResizable = false
                stage.sizeToScene()
            } else {
                stage.isResizable = true
            }
            stage.centerOnScreen()
        } catch (e: IOException) {
            println("Error: ${e.message}")
        }
    }
}