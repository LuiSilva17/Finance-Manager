package com.financemanager.controllers

import com.financemanager.IO.importers.BankStatementParser
import com.financemanager.IO.importers.CGDParser
import com.financemanager.IO.importers.CreditoAgricolaParser
import com.financemanager.service.AccountManager
import com.financemanager.service.SettingsManager
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import java.io.File
import java.io.IOException

class CreateManagerController {

    @FXML var back_to_menu_button: Button = Button()
    @FXML var import_file_path_button: Button = Button()
    @FXML var import_file_button: Button = Button()
    @FXML var file_name_label: Label = Label()

    var filePath: String = ""

    @FXML
    fun handleBacktoMenu(event: ActionEvent) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/Menu.fxml"))
            val settingsRoot = loader.load<Parent>()

            val menuScene = Scene(settingsRoot)

            val currentStage = (event.source as Node).scene.window as Stage

            currentStage.apply {
                scene = menuScene
                title = "Menu"
                show()
            }
        } catch (e: IOException) {
            println("Error loading FXML: ${e.message}")
        }
    }

    @FXML
    fun handleImportMenu(event: ActionEvent) {
        val fileChooser = FileChooser()
        fileChooser.title = "Import New Manager"

        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("Manager Files (*.manager)", "*.csv", "*.xlsx"),
            FileChooser.ExtensionFilter("All Files", "*.*")
        )
        val currentStage = (event.source as Node).scene.window as Stage

        val selectedFile: File? = fileChooser.showOpenDialog(currentStage)
        filePath = selectedFile?.absolutePath ?: ""

        if (selectedFile != null) {
            file_name_label.setText(selectedFile.name)
            println("Selected File: ${selectedFile.absolutePath}")
        } else {
            println("Selection Cancelled")
        }
    }

    @FXML
    fun handleImportManager(event: ActionEvent) {
        if (filePath.isEmpty()) {
            Alert(Alert.AlertType.ERROR).apply {
                title = "Selection Error"
                headerText = null
                contentText = "Please select a valid .manager file before proceeding."
                showAndWait()
            }
            return
        } else {
            val selectedBank = showBankSelectionDialog() ?: return
            val parser = when(selectedBank) {
                "Crédito Agricola" -> CreditoAgricolaParser();
                "CGD" -> CGDParser();
                else -> CreditoAgricolaParser()
            }
            val file: File = File(filePath)
            val trimmedName = file!!.nameWithoutExtension

            try {
                val transactions = parser.parse(file)
                val newManager: AccountManager = AccountManager(trimmedName)
                newManager.bankType = selectedBank
                newManager.loadTransactions(transactions)
                newManager.saveToFile()
                navigateToDashboard(event, newManager)
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }

        }
    }

    private fun showBankSelectionDialog(): String? {
        val dialog = Dialog<String>()
        dialog.title = "Create New Manager"

        val okButtonType = ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(okButtonType, ButtonType.CANCEL)

        val comboBanks = ComboBox<String>()

        val refreshBanks = {
            //val installed = SettingsManager.getInstance().installedBanks
            val installed = listOf("Crédito Agrícola", "CGD")
            comboBanks.items = FXCollections.observableArrayList(installed)
            comboBanks.selectionModel.selectFirst()
            //comboBanks.items = FXCollections.observableArrayList(*installed)
        }
        refreshBanks()

        val btnSettings = Button("⚙️").apply {
            setOnAction {
                openSettings()
                refreshBanks()
            }
        }

        val layout = VBox(10.0).apply {
            padding = Insets(20.0)
            children.addAll(
                HBox(btnSettings),
                Label("Select the Bank for this new Manager:"),
                comboBanks.apply { maxWidth = Double.MAX_VALUE }
            )
        }

        dialog.dialogPane.content = layout
        dialog.resultConverter = Callback { if (it == okButtonType) comboBanks.value else null }

        return dialog.showAndWait().orElse(null)
    }

    private fun openSettings() {
        try {
            val loader = FXMLLoader(javaClass.getResource("/SettingsView.fxml"))
            val root = loader.load<Parent>()

            val stage = Stage()
            stage.title = "Bank Reader Settings"
            stage.scene = Scene(root)

            stage.initModality(Modality.APPLICATION_MODAL)

            stage.showAndWait()
        } catch (e: Exception) {
            println("Error opening Settings: ${e.message}")
        }
    }

    private fun navigateToDashboard(event: ActionEvent, manager: AccountManager) {
        val loader = FXMLLoader(javaClass.getResource("/Dashboard.fxml"))

        val root = loader.load<Parent>()

        val controller = loader.getController<DashboardController>()

        controller.initData(manager)

        val stage = (event.source as Node).scene.window as Stage
        val scene = Scene(root, 1200.0, 800.0)
        stage.scene = scene

        stage.isResizable = true
        stage.centerOnScreen()
        stage.show()
    }
}