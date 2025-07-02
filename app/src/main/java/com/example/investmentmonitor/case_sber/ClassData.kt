package com.example.investmentmonitor.case_sber

// Класс для хранения данных:
data class CaseAll(
    val name: String,
    val isin: String,
    val ticker: String,
    val currency: String,
    val quantity: Int,
    val category: String,
    var price: Double,
    var price_prev: Double, // цена закрытия предыдущего дня
    var price_open: Double, // цена открытия торгового дня
    var price_last: Double, // цена последней сделки
    var decimals: Int,
    val account: String
)

// Класс для хранения данных:
data class Assets(
    val id: Int,
    val name: String,
    val isin: String,
    val ticker: String,
    val currency: String,
    val quantity: Int,
    val category: String,
    val price: Double,
    var price_prev: Double,
    var price_open: Double,
    var price_last: Double,
    var decimals: Int,
    val account: String
)

// Класс для хранения данных:
data class Markets(
    val date_conclusion: String,
    val time: String,
    val name: String,
    val code: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val account: String
)

// Класс для хранения данных:
data class Guides(
    val code: String,
    val isin: String
)

// Класс для хранения данных:
data class FilesHTML(
    val id: Int,
    val name: String
)

// Класс для хранения данных:
data class Payments(
    val id: Int,
    val date: String,
    val marketType: String,
    val operations: String,
    val currency: String,
    val credit: Double,
    val account: String
)

// Класс для хранения данных:
data class FreeCash(
    val id: Int,
    val RUB: String,
    val USD: String,
    val EUR: String,
    val CNY: String,
    val marketType: String,
    val account: String
)
