package com.financemanager.controllers

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class MainApp : Application() {
    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/Menu.fxml"))

        primaryStage.apply {
            title = "Finance Manager"
            scene = Scene(root)
            isResizable = false
            show()
        }
    }
}

fun main() {
    Application.launch(MainApp::class.java)
}