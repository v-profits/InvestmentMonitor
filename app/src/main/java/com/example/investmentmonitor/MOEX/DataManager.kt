package com.example.investmentmonitor.MOEX

var currentPosition: Int? = null // Переменная для хранения позиции выбранного элемента для смены цвета флага компании
var titleItem: String = "Заголовок" // заголовок в итеме по умолчанию

var isResetTickers = false // не сбросить все изменения
var isNewFiles = false // новизна файла брокера

var isSecurities = false
var isMarketData = false

var isSave = true // дает разрешение на сохранение в pref после завершения циклов перемещения итемов

//var accoutName = "" // аккаунт отчета брокера

var nameFragment = "" // для выбора вызова фрагмента
var tv_title = "" // титл для каждого фрагмента

var boardList = mutableListOf<String>()

val flagsMap = mutableMapOf(
    "TQBR" to false, "SMAL" to false, "SPEQ" to false, // акции
    "TQTF" to false, "TQPI" to false, "TQIF" to false, // ПИФ
    "TQOB" to false, "TQCB" to false, "PACT" to false, "SPOB" to false,
    "TQIR" to false, "TQIY" to false, "TQOD" to false, "TQOE" to false,
    "TQOY" to false, "TQRD" to false, "TQUD" to false, // ОФЗ
    "SNDX" to false, "RTSI" to false, "INAV" to false, "INPF" to false, // индексы
    "RFUD" to false, // фьючерсы
//    "ROPD" to false, // опционы
)

// список
var backupTicker = mutableListOf<TickerResponse>()
// резервный список
var zipTicker = mutableListOf<TickerResponse>()
// биржевой список
var moexTicker = mutableListOf<TickerResponse>()

// список торговых кодов Фондового рынока Срочного рынока
val tradingCodeStockMarket = mutableListOf<String>("40467CZ","S04UL15")

// список торговых кодов Фондового рынока Срочного рынока
val tradingCodeFuturesMarket = mutableListOf<String>("A728uy6")