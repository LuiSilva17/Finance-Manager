package com.financemanager.controllers

import com.financemanager.service.AccountManager
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.io.IOException

class LoadManagerController {

    @FXML lateinit var back_to_menu_button: Button
    @FXML lateinit var editButton: Button
    @FXML lateinit var emptyStateView: VBox
    @FXML lateinit var managerListView: VBox

    private var isEditMode = false

    @FXML
    fun initialize() {
        // Configura a janela (Stage) assim que o controlador ganha acesso à Scene [cite: 2026-01-24]
        back_to_menu_button.sceneProperty().addListener { _, _, newScene ->
            newScene?.windowProperty()?.addListener { _, _, newWin ->
                (newWin as? Stage)?.let { stage ->
                    stage.isResizable = false
                    stage.sizeToScene()
                    stage.centerOnScreen()
                }
            }
        }
        refreshManagerList()
    }

    /**
     * Alterna entre o modo de seleção e o modo de edição (Rename/Delete) [cite: 2026-01-24]
     */
    @FXML
    fun handleToggleEdit(event: ActionEvent) {
        isEditMode = !isEditMode
        editButton.text = if (isEditMode) "Done" else "Edit"
        refreshManagerList()
    }

    /**
     * reconstrói a lista de managers na VBox central
     */
    private fun refreshManagerList() {
        val path = System.getProperty("user.home") + File.separator + "Finance Manager Data" + File.separator + "Managers"
        val folder = File(path)
        if (!folder.exists()) folder.mkdirs()

        val managerFiles = folder.listFiles { _, name -> name.endsWith(".manager") }

        if (managerFiles != null && managerFiles.isNotEmpty()) {
            toggleViews(true)
            managerListView.children.clear()
            managerListView.spacing = 15.0

            managerFiles.forEach { file ->
                val managerName = file.nameWithoutExtension

                // HBox para conter os botões da linha (Simula o rowPanel do Swing)
                val row = HBox(10.0).apply {
                    alignment = Pos.CENTER
                }

                // --- BOTÃO RENAME (Só aparece em Edit Mode) ---
                if (isEditMode) {
                    val btnRename = Button("✎").apply {
                        style = "-fx-background-color: #FFA500; -fx-text-fill: white; -fx-font-weight: bold;"
                        prefWidth = 45.0
                        prefHeight = 45.0
                        setOnAction { handleRename(file) }
                    }
                    row.children.add(btnRename)
                }

                // --- BOTÃO DO MANAGER ---
                val btn = Button(managerName).apply {
                    prefWidth = if (isEditMode) 200.0 else 300.0
                    prefHeight = 45.0
                    isDisable = isEditMode // Desativa cliques para abrir enquanto edita
                    setOnAction {
                        // CHAMADA DE INSTÂNCIA COM OBJETO FILE [cite: 2026-01-24]
                        val am = AccountManager(managerName)
                        val loadedManager = am.loadFromFile(file)
                        openDashboard(loadedManager)
                    }
                }
                row.children.add(btn)

                // --- BOTÃO DELETE (Só aparece em Edit Mode) ---
                if (isEditMode) {
                    val btnDelete = Button("X").apply {
                        style = "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;"
                        prefWidth = 45.0
                        prefHeight = 45.0
                        setOnAction { handleDelete(file) }
                    }
                    row.children.add(btnDelete)
                }

                managerListView.children.add(row)
            }
        } else {
            toggleViews(false)
            isEditMode = false
            editButton.text = "Edit"
        }
    }

    private fun handleRename(file: File) {
        val dialog = TextInputDialog(file.nameWithoutExtension)
        dialog.title = "Rename Manager"
        dialog.headerText = "Renaming '${file.nameWithoutExtension}'"
        dialog.contentText = "New name:"

        dialog.showAndWait().ifPresent { newName ->
            if (newName.isNotBlank()) {
                val newFile = File(file.parent, "$newName.manager")
                if (file.renameTo(newFile)) {
                    // Instanciamos e carregamos do novo ficheiro para atualizar o nome interno [cite: 2026-01-24]
                    val am = AccountManager(newName)
                    val loaded = am.loadFromFile(newFile)
                    loaded.setName(newName) // Sincroniza o campo name
                    loaded.saveToFile()      // Atualiza o ficheiro com o novo estado
                    refreshManagerList()
                }
            }
        }
    }

    private fun handleDelete(file: File) {
        val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Delete Manager"
            headerText = "Confirm deletion of '${file.nameWithoutExtension}'"
            contentText = "This will permanently remove the manager file from your Fedora."
        }

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (file.delete()) refreshManagerList()
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
            // CHAMADA DE INSTÂNCIA COM OBJETO FILE [cite: 2026-01-24]
            val am = AccountManager(file.nameWithoutExtension)
            val imported = am.loadFromFile(file)
            openDashboard(imported)
        }
    }

    @FXML
    fun handleCreate(event: ActionEvent) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/CreateManager.fxml"))
        navigateTo(root, "Create New Manager", resizable = false)
    }

    private fun openDashboard(manager: AccountManager?) {
        if (manager == null) return
        val loader = FXMLLoader(javaClass.getResource("/Dashboard.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<DashboardController>()
        controller.initData(manager)
        navigateTo(root, "Dashboard", resizable = true)
    }

    @FXML
    fun handleBacktoMenu(event: ActionEvent) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/Menu.fxml"))
        navigateTo(root, "Menu", resizable = false)
    }

    private fun navigateTo(root: Parent, titleStr: String, resizable: Boolean) {
        val stage = back_to_menu_button.scene.window as Stage
        stage.scene = Scene(root)
        stage.title = titleStr
        stage.isResizable = resizable
        if (!resizable) stage.sizeToScene()
        stage.centerOnScreen()
    }
}