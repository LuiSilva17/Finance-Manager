package com.financemanager.controllers

import com.financemanager.IO.importers.CGDParser
import com.financemanager.IO.importers.CreditoAgricolaParser
import com.financemanager.model.Transaction
import com.financemanager.model.TransactionEnum
import com.financemanager.service.AccountManager
import com.financemanager.service.CategoryManager
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardController {

    private lateinit var manager: AccountManager

    // Elementos de UI
    @FXML lateinit var bankNameLabel: Label
    @FXML lateinit var balanceLabel: Label

    @FXML lateinit var transactionTable: TableView<Transaction>
    @FXML lateinit var dateColumn: TableColumn<Transaction, LocalDate>
    @FXML lateinit var typeColumn: TableColumn<Transaction, TransactionEnum>
    @FXML lateinit var valueColumn: TableColumn<Transaction, java.math.BigDecimal>
    @FXML lateinit var categoryColumn: TableColumn<Transaction, String>
    @FXML lateinit var descriptionColumn: TableColumn<Transaction, String>

    @FXML lateinit var addFileButton: Button
    @FXML lateinit var categoriesButton: Button
    @FXML lateinit var backToMenuButton: Button

    fun initData(manager: AccountManager) {
        this.manager = manager
        setupTable()
        refreshUI()
    }

    private fun setupTable() {
        dateColumn.cellValueFactory = PropertyValueFactory("date")
        valueColumn.cellValueFactory = PropertyValueFactory("value")
        descriptionColumn.cellValueFactory = PropertyValueFactory("description")

        // Lógica de categorias dinâmica baseada na descrição
        categoryColumn.setCellValueFactory { cellData ->
            val transaction = cellData.value
            val categoryName = CategoryManager.getInstance().getCategoryFor(transaction.description)
            SimpleStringProperty(categoryName ?: "Uncategorized")
        }

        typeColumn.cellValueFactory = PropertyValueFactory("type")

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Formatação de data na coluna
        dateColumn.setCellFactory { _ ->
            object : TableCell<Transaction, LocalDate>() {
                override fun updateItem(item: LocalDate?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) null else formatter.format(item)
                }
            }
        }
    }

    /**
     * Atualiza todos os elementos visuais com os dados atuais do manager.
     */
    private fun refreshUI() {
        bankNameLabel.text = manager.name
        val balance = manager.currentBalance
        balanceLabel.text = String.format("%.2f €", balance)

        if (balance.signum() >= 0) {
            balanceLabel.style = "-fx-text-fill: green; -fx-font-weight: bold;"
        } else {
            balanceLabel.style = "-fx-text-fill: red; -fx-font-weight: bold;"
        }

        transactionTable.items = FXCollections.observableArrayList(manager.transactions)
        transactionTable.sortOrder.add(dateColumn)
    }

    @FXML
    fun handleCategories(event: ActionEvent) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/CategoriesView.fxml"))
            val root = loader.load<Parent>()

            val categoriesStage = Stage().apply {
                title = "Categories"
                scene = Scene(root)
                initModality(Modality.APPLICATION_MODAL)
                initOwner((event.source as Node).scene.window)
            }
            categoriesStage.showAndWait()
            refreshUI()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @FXML
    fun handleAddFile(event: ActionEvent) {
        val stage = (event.source as Node).scene.window as Stage

        // 1. Garante que o tipo de banco está definido
        if (manager.bankType == null) {
            val banks = listOf("Crédito Agrícola", "CGD")
            val dialog = ChoiceDialog(banks[0], banks)
            dialog.title = "Select Bank Type"
            dialog.headerText = "Identify the bank statement format."
            dialog.contentText = "Bank:"

            val result = dialog.showAndWait()
            if (result.isPresent) {
                manager.bankType = result.get()
                manager.saveToFile()
            } else return
        }

        // 2. Seleção de ficheiro
        val fileChooser = FileChooser().apply {
            title = "Open Bank Statement"
            extensionFilters.add(FileChooser.ExtensionFilter("Statement Files", "*.csv", "*.xlsx", "*.xls"))
        }

        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            try {
                // 3. Parser e Merge
                val parser = if (manager.bankType == "CGD") CGDParser() else CreditoAgricolaParser()
                val newTransactions = parser.parse(file)

                manager.mergeTransactions(newTransactions)
                manager.saveToFile()

                refreshUI()
            } catch (e: Exception) {
                val alert = Alert(Alert.AlertType.ERROR, "Failed to import: ${e.message}")
                alert.showAndWait()
            }
        }
    }

    @FXML
    fun handleBackToMenu(event: ActionEvent) {
        try {
            val loader = FXMLLoader(javaClass.getResource("/Menu.fxml"))
            val root = loader.load<Parent>()
            val stage = (event.source as Node).scene.window as Stage

            stage.isResizable = false
            stage.scene = Scene(root)
            stage.sizeToScene()
            stage.centerOnScreen()
            stage.title = "Finance Manager - Menu"
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}