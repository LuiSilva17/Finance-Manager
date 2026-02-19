package com.financemanager.controllers

import com.financemanager.service.AccountManager
import javafx.fxml.FXML

class DashboardController {

    private lateinit var manager: AccountManager

    // Método chamado pelo menu para injetar o objeto
    fun initData(manager: AccountManager) {
        this.manager = manager

        // Exemplo: Atualizar o título da Dashboard ou carregar a tabela
        println("Dashboard carregada com sucesso para: ${manager.name}")

        // Podes chamar aqui funções para preencher a UI
        // setupTransactionTable()
    }

    @FXML
    fun initialize() {
        // O initialize corre ANTES do initData.
        // Por isso, aqui ainda não tens acesso ao 'manager'.
    }

}