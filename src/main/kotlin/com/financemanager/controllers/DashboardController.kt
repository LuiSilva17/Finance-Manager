package com.financemanager.controllers

import com.financemanager.model.Transaction
import com.financemanager.model.TransactionEnum
import com.financemanager.service.AccountManager
import com.financemanager.service.CategoryManager
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableCell
import javafx.scene.control.cell.PropertyValueFactory
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

}