package com.example.investmentmonitor.case_sber

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Методы

// Метод для очистки всех таблиц
@Synchronized
fun DatabaseHelper.clearTable() {
    val listTableName = listOf(
        "Assets",
        "Stocks",
        "Contracts",
        "FilesHTML",
        "MainMarket",
        "FuturesMarket",
        "REPOTransactions",
        "CentralBankMove",
        "CashFlow",
        "Payments",
        "FreeCash",
        "Guide"
    )
    for (tableName in listTableName) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $tableName")
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = ?", arrayOf(tableName)) // Сбрасываем автоинкремент
        Log.e("aaa", "Таблица $tableName очищена")
    }
}

// Метод для очистки одной таблицы
@Synchronized
fun DatabaseHelper.clearTableName(name: String) {
    val db = writableDatabase
    db.execSQL("DELETE FROM $name")
    db.execSQL("DELETE FROM sqlite_sequence WHERE name = ?", arrayOf(name)) // Сбрасываем автоинкремент
}

//проверяем наличие файлов отчетов уже в таблице имен файлов "FilesHTML" для исключения дублирования
@Synchronized
fun DatabaseHelper.checkDuplicateFiles(name: String): Boolean {
    val db = readableDatabase
    val cursor = db.rawQuery("SELECT name FROM FilesHTML", null)
    val files = mutableListOf<String>()
    if (cursor.moveToFirst()) {
        do {
            files.add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) // считываем из таблицы имена отчетов брокера
        } while (cursor.moveToNext())
    }
    cursor.close()
    for (file in files)
        if (file == name)  // проверяем наличие такого отчета в таблице
            return false // пропускаем этот отчет
    return true // этот отчет добавляем в таблицу с проверкой
}

// функция проверяет дублирование записей в таблице по одной колонке
@Synchronized
fun DatabaseHelper.isTableExistsOneColumn(nameTable: String, column: String, value: String): Boolean {
    val db = this.readableDatabase
    val cursor = db.rawQuery("SELECT 1 FROM $nameTable WHERE $column = ?", arrayOf(value))
    val exists = cursor.moveToFirst() // Проверяем, есть ли совпадения
    cursor.close()
    return exists
}

// функция проверяет дублирование записей в таблице по несколько колонкам, кроме последней
@Synchronized
fun DatabaseHelper.isRowExistsNotAccount(
    nameTable: String,
    columns: List<String>,
    values: List<Any> // Используем Any для разных типов
): Boolean {
    if (columns.size != values.size) {
        throw IllegalArgumentException("Количество столбцов и значений должно совпадать")
    }
    val db = this.readableDatabase
    // Проверяем совпадение всех колонок, кроме последней
    val partialWhereClause = columns.dropLast(1).joinToString(" AND ") { "$it = ?" }
    val partialValueStrings = values.dropLast(1).map { it.toString() }
    val partialCursor = db.rawQuery("SELECT 1 FROM $nameTable WHERE $partialWhereClause", partialValueStrings.toTypedArray())
    val partialExists = partialCursor.moveToFirst()
    partialCursor.close()
    // Если полное совпадение найдено, то дублирование отсутствует
    if (partialExists) {
        // Проверяем полное совпадение всех колонок
        val fullWhereClause = columns.joinToString(" AND ") { "$it = ?" }
        val fullValueStrings = values.map { it.toString() }
        val fullCursor = db.rawQuery("SELECT 1 FROM $nameTable WHERE $fullWhereClause", fullValueStrings.toTypedArray())
        val fullExists = fullCursor.moveToFirst()
        fullCursor.close()
        // Если полное совпадение найдено, то дублирование отсутствует
        if (fullExists) return false
    }
    // Если совпадение найдено, это дублирование
    return partialExists
}

// функция проверяет дублирование записей в таблице по несколько колонкам
@Synchronized
fun DatabaseHelper.isRowExistsAllColumns(
    nameTable: String,
    columns: List<String>,
    values: List<Any> // Используем Any для разных типов
): Boolean {
    if (columns.size != values.size) {
        throw IllegalArgumentException("Количество столбцов и значений должно совпадать")
    }
    val db = this.readableDatabase
    val whereClause = columns.joinToString(" AND ") { "$it = ?" }
    val valueStrings = values.map { it.toString() }
    val cursor = db.rawQuery("SELECT 1 FROM $nameTable WHERE $whereClause", valueStrings.toTypedArray())
    val exists = cursor.moveToFirst()
    cursor.close()
    return exists
}

// Импорт данных из отчета
@Synchronized
fun DatabaseHelper.importTablesFromReport(htmlFilePath: String) {
    val file = File(htmlFilePath)
    if (!file.exists()) {
        Log.e("aaa", "Файл не найден: $htmlFilePath")
        return
    }
    val document: Document = Jsoup.parse(file, "UTF-8")
    // Находим таблицу после заголовка "Торговый код: 40467CZ"
    val elements = document.body().children() // весь отчет брокера
    // создаем списки для хранения строк таблиц
    val resultStocks = mutableListOf<Element>() // здесь сохраним искомые таблицы Stocks
    val resultContract = mutableListOf<Element>() // здесь сохраним искомые таблицы Contracts
    val resultMainMarket = mutableListOf<Element>() // здесь сохраним искомые таблицы MainMarket
    val resultFuturesMarket = mutableListOf<Element>() // здесь сохраним искомые таблицы FuturesMarket
    val resultREPOTransactions = mutableListOf<Element>() // здесь сохраним искомые таблицы REPOTransactions
    val resultCentralBankMove = mutableListOf<Element>() // здесь сохраним искомые таблицы CentralBankMove
    val resultCashFlow = mutableListOf<Element>() // здесь сохраним искомые таблицы CashFlow
    val resultPayments = mutableListOf<Element>() // здесь сохраним искомые таблицы Payments
    val resultGuide = mutableListOf<Element>() // здесь сохраним искомые таблицы Guide
    val resultAssets = mutableListOf<Element>() // здесь сохраним искомые таблицы Assets

    var accountStocks = ""
    var accountContracts = ""
    var account = ""
    // Извлечение имени файла из пути патча
    val fileName = htmlFilePath.substringAfterLast('/')

    // сохраняем таблицы в отдельные списки
    elements.forEachIndexed { index, element ->
        account = fileName.substringBefore("_") // Извлекаем все символы до первого _ (имя аккаунта)
        if (element.tagName() == "p" && element.select("br").isNotEmpty()) {
            val text = element.text().trim()
            if (isFileNew(fileName)) { // если файл свежий по дате то обновляем портфель
                if (text.startsWith("Портфель Ценных Бумаг") && index + 1 < elements.size) {
                    // Удаляем лишние пробелы в начале и конце строки, разделяем по пробелам и сохраняем последнее слово
                    accountStocks = elements[index].text().trim().split("\\s+".toRegex()).last()
                    // удаляет строки из таблицы на основе заданного условия (WHERE account = ?)
                    deleteTableByAccount("Assets", account)
                    deleteTableByAccount("Stocks", account)
                    resultAssets.add(elements[index + 1]) // Сохраняем таблицу в список
                    resultStocks.add(elements[index + 1]) // Сохраняем таблицу в список
                } else if (text.startsWith("Срочный рынок ") && index + 1 < elements.size) {
                    // Удаляем лишние пробелы в начале и конце строки, разделяем по пробелам и сохраняем последнее слово
                    accountContracts = elements[index].text().trim().split("\\s+".toRegex()).last()
                    // удаляет строки из таблицы на основе заданного условия (WHERE account = ?)
                    deleteTableByAccount("Assets", account)
                    deleteTableByAccount("Contracts", account)
                    resultAssets.add(elements[index + 1]) // Сохраняем таблицу в список
                    resultContract.add(elements[index + 1]) // Сохраняем таблицу в список
                }
            }
            if (text == "Сделки купли/продажи ценных бумаг" && index + 1 < elements.size) {
                resultMainMarket.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Срочные сделки" && index + 1 < elements.size) {
                resultFuturesMarket.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Сделки РЕПО" && index + 1 < elements.size) {
                resultREPOTransactions.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Движение ЦБ, не связанное с исполнением сделок" && index + 1 < elements.size) {
                resultCentralBankMove.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Движение денежных средств за период" && index + 1 < elements.size) {
                resultCashFlow.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Выплаты дохода от эмитента на внешний счет" && index + 1 < elements.size) {
                resultPayments.add(elements[index + 1]) // Сохраняем таблицу в список
            } else if (text == "Справочник Ценных Бумаг" && index + 1 < elements.size) {
                resultGuide.add(elements[index + 1]) // Сохраняем таблицу в список
            }
        }
    }

    // активы портфеля - выборка по другим таблицам
    if (resultAssets.isNotEmpty()) {
        if (accountStocks != "") {
//        Log.e("aaa", "Таблицы: 'Портфель Ценных Бумаг Торговый код:'")
            // Обрабатываем строки таблицы
            val tableRows = resultStocks.first().select("tr")
            for (row in tableRows) {
                val columns = row.select("td")
                if (columns.size < 18) continue // Пропускаем некорректные строки
                val columnText = columns[1].text().trim() // Получаем текст из второй ячейки
                if (!columnText.startsWith("RU")) continue // Пропускаем некорректные строки
                val name = columns[0].text().trim()
                val isin = columns[1].text().trim()
                val ticker = ""
                val currency = columns[2].text().trim()
                val quantity =
                    columns[17].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull()
                        ?: 0
                val category = ""
                val price = 0.0

                val price_prev = 0.0
                val price_open = 0.0
                val price_last = 0.0
                val decimals = 0

                // Добавляем импортированные данные в таблицу
                addAssets(name, isin, ticker, currency, quantity, category, price,
                    price_prev, price_open, price_last, decimals, account)
            }
        } else if (accountContracts != "") {
//        Log.e("aaa", "Таблицы: 'Срочный рынок Торговый код:'")
            // Обрабатываем строки таблицы
            val tableRows = resultContract.first().select("tr")
            for (row in tableRows) {
                val columns = row.select("td")
                if (columns.size < 9) continue // Пропускаем некорректные строки
                // Проверяем, что строка достаточно длинная и третий символ - "."
                val text = columns[3].text().trim()
                if (text.length < 3 || text[2] != '.')
                    continue // Пропускаем некорректные строки
                val name = columns[0].text().trim()
                val isin = columns[1].text().trim()
                val ticker = columns[1].text().trim()
                val currency = columns[2].text().trim()
                val quantity =
                    columns[6].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull()
                        ?: 0
                val category = ""
                val price = 0.0

                val price_prev = 0.0
                val price_open = 0.0
                val price_last = 0.0
                val decimals = 0

                // Добавляем импортированные данные в таблицу
                addAssets(name, isin, ticker, currency, quantity, category, price,
                    price_prev, price_open, price_last, decimals, account)
            }
        }
    }
    // акции, облигации, фонды
    if (resultStocks.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Портфель Ценных Бумаг Торговый код:'")
        // Обрабатываем строки таблицы
        val tableRows = resultStocks.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 18) continue // Пропускаем некорректные строки
            val columnText = columns[1].text().trim() // Получаем текст из второй ячейки
            if (!columnText.startsWith("RU")) continue // Пропускаем некорректные строки
            val name = columns[0].text().trim()
            val isin = columns[1].text().trim()
            val currency = columns[2].text().trim()
            val quantity = columns[17].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            // Добавляем импортированные данные в таблицу
            addStocks(name, isin, currency, quantity, account)
        }
    }
    // фьючерсы
    if (resultContract.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Срочный рынок Торговый код:'")
        // Обрабатываем строки таблицы
        val tableRows = resultContract.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 9) continue // Пропускаем некорректные строки
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[3].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val code = columns[0].text().trim()
            val type = columns[1].text().trim()
            val quantity = columns[6].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            val date = columns[3].text().trim()
            // Добавляем импортированные данные в таблицу
            addContracts(code, type, quantity, date, account)
        }
    }
    // "Сделки купли/продажи ценных бумаг"
    if (resultMainMarket.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Сделки купли/продажи ценных бумаг'")
        // Обрабатываем строки таблицы
        val tableRows = resultMainMarket.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 16) continue // Пропускаем некорректные строки
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[0].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim()
            val v3 = columns[2].text().trim()
            val v4 = columns[3].text().trim()
            val v5 = columns[4].text().trim()
            val v6 = columns[5].text().trim()
            val v7 = columns[6].text().trim()
            val v8 = columns[7].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            val v9 = columns[8].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v10 = columns[9].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v11 = columns[10].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v12 = columns[11].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v13 = columns[12].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v14 = columns[13].text().trim()
            val v15 = columns[14].text().trim()
            // Добавляем импортированные данные в таблицу
            if (!isTableExistsOneColumn("MainMarket", "transaction_number", v14)) // проверяем на дублирование по transaction_number
                addMainMarket(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, account)
        }
    }
    // "Срочные сделки"
    if (resultFuturesMarket.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Срочные сделки'")
        // Обрабатываем строки таблицы
        val tableRows = resultFuturesMarket.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 12) continue // Пропускаем некорректные строки
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[0].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim()
            val v3 = columns[2].text().trim()
            val v4 = columns[3].text().trim()
            val v5 = columns[4].text().trim()
            val v6 = columns[5].text().trim()
            val v7 = columns[6].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            val v8 = columns[7].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v9 = columns[8].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v10 = columns[9].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v11 = columns[10].text().trim()
            val v12 = columns[11].text().trim()
            // Добавляем импортированные данные в таблицу
            if (!isTableExistsOneColumn("FuturesMarket", "transaction_number", v11)) // проверяем на дублирование по transaction_number
                addFuturesMarket(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, account)
        }
    }
    // "Сделки РЕПО"
    if (resultREPOTransactions.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Сделки РЕПО'")
        // Обрабатываем строки таблицы
        val tableRows = resultREPOTransactions.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 23) continue // Пропускаем некорректные строки
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[0].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim()
            val v3 = columns[2].text().trim()
            val v4 = columns[3].text().trim()
            val v5 = columns[4].text().trim()
            val v6 = columns[5].text().trim()
            val v7 = columns[6].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            val v8 = columns[7].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v9 = columns[8].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v10 = columns[9].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v11 = columns[10].text().trim()
            val v12 = columns[11].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v13 = columns[12].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v14 = columns[13].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v15 = columns[14].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v16 = columns[15].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v17 = columns[16].text().trim()
            val v18 = columns[17].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0
            val v19 = columns[18].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v20 = columns[19].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v21 = columns[20].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v22 = columns[21].text().trim()
            // Добавляем импортированные данные в таблицу
            if (!isTableExistsOneColumn("REPOTransactions", "transaction_number", v22)) // проверяем на дублирование по transaction_number
                addREPOTransactions(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, account)
        }
    }
    // "Движение ЦБ, не связанное с исполнением сделок"
    if (resultCentralBankMove.isNotEmpty()) {
        // Обрабатываем строки таблицы
        val tableRows = mutableListOf<Element>() // Список для хранения всех строк из обеих таблиц
        for (table in resultCentralBankMove) {
            val rows = table.select("tr") // Получаем строки из текущей таблицы
            tableRows.addAll(rows) // Добавляем все строки в общий список
        }
        val currentReportId: String = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) // Уникальный идентификатор текущего отчета
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 11) continue
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[6].text().trim()
            if (text.length < 3 || text[2] != '.') continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата операции
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim() // Наименование ЦБ
            val v3 = columns[2].text().trim() // Код ЦБ
            val v4 = columns[3].text().trim() // Вид
            val v5 = columns[4].text().trim() // Основание операции
            val v6 = columns[5].text().replace("\\s".toRegex(), "").replace(",", "").toIntOrNull() ?: 0 // Количество, шт.
            val v7 = columns[6].text().trim().split(".").let { parts -> // Дата приобретения
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v8 = columns[7].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0 // Цена
            if (!isRowExistsNotAccount("CentralBankMove",
                    listOf("date_operation", "name", "code", "type", "operations", "quantity", "date_buy", "price", "account", "report_id"),
                    listOf(v1, v2, v3, v4, v5, v6, v7, v8, account, currentReportId))) { // проверяем на дублирование, с несовпадением последнего параметра

                addCentralBankMove(v1, v2, v3, v4, v5, v6, v7, v8, currentReportId, account)
            }
        }
    }
    // "Движение денежных средств за период"
    if (resultCashFlow.isNotEmpty()) {
        // Обрабатываем строки таблицы
        val tableRows = mutableListOf<Element>() // Список для хранения всех строк из обеих таблиц
        for (table in resultCashFlow) {
            val rows = table.select("tr") // Получаем строки из текущей таблицы
            tableRows.addAll(rows) // Добавляем все строки в общий список
        }
        val currentReportId: String = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) // Уникальный идентификатор текущего отчета
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 6) continue
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[0].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim() // Торговая площадка
            val v3 = columns[2].text().trim() // Описание операции
            val v4 = columns[3].text().trim() // Валюта
            val v5 = columns[4].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            val v6 = columns[5].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            if (!isRowExistsNotAccount("CashFlow",
                    listOf("date", "marketType", "operations", "currency", "credit", "debit", "account", "report_id"),
                    listOf(v1, v2, v3, v4, v5, v6, account, currentReportId))) { // проверяем на дублирование, с несовпадением последнего параметра

                addCashFlow(v1, v2, v3, v4, v5, v6, currentReportId, account)
            }
        }
    }
    // "Выплаты дохода от эмитента на внешний счет"
    if (resultPayments.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Выплаты дохода от эмитента на внешний счет'")
        // Обрабатываем строки таблицы
        val tableRows = resultPayments.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 5) continue
            // Проверяем, что строка достаточно длинная и третий символ - "."
            val text = columns[0].text().trim()
            if (text.length < 3 || text[2] != '.')
                continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim().split(".").let { parts -> // Дата
                "${parts[2]}-${parts[1]}-${parts[0]}" // Преобразуем дату в формате DD.MM.YYYY в формат YYYY-MM-DD
            }
            val v2 = columns[1].text().trim() // Торговая площадка
            val v3 = columns[2].text().trim() // Описание операции
            val v4 = columns[3].text().trim() // Валюта
            val v5 = columns[4].text().replace("\\s".toRegex(), "").replace(",", "").toDoubleOrNull() ?: 0.0
            if (!isRowExistsAllColumns("CashFlow",
                    listOf("date", "marketType", "operations", "currency", "credit", "account"),
                    listOf(v1, v2, v3, v4, v5, account))) { // проверяем на дублирование, с совпадением по колонкам

                addPayments(v1, v2, v3, v4, v5, account)
            }
        }
    }
    // справочник эмитентов портфеля
    if (resultGuide.isNotEmpty()) {
//        Log.e("aaa", "Таблицы: 'Справочник Ценных Бумаг'")
        // Обрабатываем строки таблицы
        val tableRows = resultGuide.first().select("tr")
        for (row in tableRows) {
            val columns = row.select("td")
            if (columns.size < 6) continue // Пропускаем некорректные строки
            val columnText = columns[2].text().trim() // Получаем текст из второй ячейки
            if (!columnText.startsWith("RU")) continue // Пропускаем некорректные строки
            val v1 = columns[0].text().trim()
            val v2 = columns[1].text().trim()
            val v3 = columns[2].text().trim()
            val v4 = columns[3].text().trim()
            val v5 = columns[4].text().trim()
            val v6 = columns[5].text().trim()
            // Добавляем импортированные данные в таблицу
            if (!isRowExistsAllColumns("Guide", listOf("isin"), listOf(v3))) // проверяем на дублирование по isin
                addGuide(v1, v2, v3, v4, v5, v6)
        }
    }

    addFilesHTML(fileName) // добавляем имя файла отчета в таблицу

    // получим отсортированный по дате и времени список таблиц MainMarket, FuturesMarket, REPOTransactions и CentralBankMove
    val markets = getSortedMainMarketEntries()
    // загружаем данные колонки category и code из таблицы Guide в таблицу Assets (category, ticker)
    updateCategoryAndTickerInAssets()
    // список парфеля разделенный по аккаунтам
    val assets = getAllAssets()
    // вычислим среднюю цену покупки для каждого эмитента и аккаунта в таблице Assets
    updateAveragePricesInAssets(assets, markets)
}

// вывод таблицы в лог
@Synchronized // доступ к базе данных синхронизирован
fun DatabaseHelper.logTableContent() {
    val listTableName = listOf(
        "Assets", // активы портфеля - выборка по другим таблицам
//        "Stocks", // акции, облигации, фонды
//        "Contracts", // фьючерсы
//        "FilesHTML", // имена файлов брокера
//        "MainMarket", // "Сделки купли/продажи ценных бумаг"
//        "FuturesMarket", // "Срочные сделки"
//        "REPOTransactions", // "Сделки РЕПО"
//        "CentralBankMove", // "Движение ЦБ, не связанное с исполнением сделок"
//        "CashFlow", // "Движение денежных средств за период"
//        "Payments", // "Выплаты дохода от эмитента на внешний счет"
//        "FreeCash", // свободные денежные средства
//        "Guide" // справочник эмитентов портфеля
    )
    for (tableName in listTableName) {
        Log.e("aaa","$tableName--------------------------------------------------------------")
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName", null)
        if (cursor.moveToFirst()) {
            do {
                // Собираем строку из значений текущей строки таблицы
                val rowData = StringBuilder("Row: ")
                for (i in 0 until cursor.columnCount) {
                    val columnName = cursor.getColumnName(i)
                    val columnValue = cursor.getString(i)
                    rowData.append("$columnName=$columnValue, ")
                }
                // Убираем последнюю запятую и пробел
                if (rowData.isNotEmpty()) rowData.setLength(rowData.length - 2)
                // Логируем строку
                Log.d("aaa", rowData.toString())
            } while (cursor.moveToNext())
        } else {
            Log.d("aaa", "Таблица $tableName пуста")
        }
        cursor.close()
    }
}

