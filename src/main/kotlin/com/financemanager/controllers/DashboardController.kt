package com.financemanager.controllers

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
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardController {

    private lateinit var manager: AccountManager

    @FXML lateinit var transactionTable: TableView<Transaction>
    @FXML lateinit var dateColumn: TableColumn<Transaction, LocalDate>
    @FXML lateinit var typeColumn: TableColumn<Transaction, TransactionEnum>
    @FXML lateinit var valueColumn: TableColumn<Transaction, Double>
    @FXML lateinit var categoryColumn: TableColumn<Transaction, String>
    @FXML lateinit var descriptionColumn: TableColumn<Transaction, String>

    @FXML var addFileButton: Button = Button()
    @FXML var categoriesButton: Button = Button()
    @FXML var backToMenuButton: Button = Button()

    fun initData(manager: AccountManager) {
        this.manager = manager

        dateColumn.cellValueFactory = PropertyValueFactory("date")
        valueColumn.cellValueFactory = PropertyValueFactory("value")
        descriptionColumn.cellValueFactory = PropertyValueFactory("description")
        categoryColumn.setCellValueFactory { cellData ->
            val transaction = cellData.value
            val categoryName = CategoryManager.getInstance().getCategoryFor(transaction.description)
            SimpleStringProperty(categoryName?: "Uncategorized")
        }
        typeColumn.cellValueFactory = PropertyValueFactory("type")

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        dateColumn.setCellFactory { _ ->
            object : TableCell<Transaction, LocalDate>() {
                override fun updateItem(item: LocalDate?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                    } else {
                        text = formatter.format(item)
                    }
                }
            }
        }
        val data = FXCollections.observableArrayList(manager.transactions)
        transactionTable.items = data

        transactionTable.sortOrder.add(dateColumn)
    }

    @FXML
    fun initialize() {
        // O initialize corre ANTES do initData.
        // Por isso, aqui ainda não tens acesso ao 'manager'.
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

    @FXML
    fun handleAddFile(event: ActionEvent) {

    }

    @FXML
    fun handleBackToMenu(event: ActionEvent) {
        val loader = FXMLLoader(javaClass.getResource("/Menu.fxml"))
        val root = loader.load<Parent>()
        val stage = (event.source as Node).scene.window as Stage
        stage.isResizable = false
        val scene = Scene(root)
        stage.scene = scene
        stage.sizeToScene()
        stage.centerOnScreen()
    }

}