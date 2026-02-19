package com.financemanager.controllers

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import java.io.IOException

class CreateManagerController {

    @FXML var back_to_menu_button: Button = Button()
    @FXML var import_file_path_button: Button = Button()
    @FXML var import_file_button: Button = Button()

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
}