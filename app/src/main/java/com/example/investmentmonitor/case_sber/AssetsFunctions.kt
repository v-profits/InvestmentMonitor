package com.example.investmentmonitor.case_sber

import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Обновляем адаптер
//@Synchronized // доступ к базе данных синхронизирован
//fun DatabaseHelper.updateCase() {
//    val db = writableDatabase
//    val assetList = getAllAssets()
//    var case = convertToCaseAll(assetList, listOf(".*\\акция\\b.*")) // Копирование всех записей таблицы Assets в класс CaseAll
//    AdapterStock(case).updateData(case) // обновляем данные в адаптере
//    case = convertToCaseAll(assetList, listOf(".*\\облигация\\b.*"))
//    AdapterBond(case).updateData(case) // обновляем данные в адаптере
//    case = convertToCaseAll(assetList, listOf(".*\\пай\\b.*", ".*\\etf\\b.*", ".*\\фонд\\b.*"))
//    AdapterFund(case).updateData(case) // обновляем данные в адаптере
//    case = convertToCaseAll(assetList, listOf(".*\\фьючерс\\b.*"))
//    AdapterFutures(case).updateData(case) // обновляем данные в адаптере
//}

// вычислим среднюю цену покупки для каждого эмитента и аккаунта в таблице Assets
@Synchronized
fun DatabaseHelper.updateAveragePricesInAssets(assets: List<Assets>, markets: List<Markets>) {
    val db = writableDatabase

//    for (market in markets)
//        if (market.name == "Белуга ао" || market.name == "НоваБев Групп (ПАО) а.о.")
//            Log.e("aaa","------ $market")
//    Log.e("aaa","================================================================================")

    for (asset in assets) {

//        val filteredEntries = markets.filter { market ->
//            market.name == asset.name &&
//                    market.account == asset.account &&
//                    ((asset.quantity > 0 && market.type == "Покупка") || (asset.quantity < 0 && market.type == "Продажа"))
//        }

        val nameAliases = mapOf(
            "РСетиЛЭ-п" to listOf("Ленэнерг-п", "РСетиЛЭ-п"),
            "Ленэнерг-п" to listOf("РСетиЛЭ-п", "Ленэнерг-п"),
            "Белуга ао" to listOf("НоваБев Групп (ПАО) а.о.", "Белуга ао"),
            "НоваБев Групп (ПАО) а.о." to listOf("Белуга ао", "НоваБев Групп (ПАО) а.о.")
        )

        val filteredEntries = markets.filter { market ->
            val possibleNames = nameAliases[asset.name] ?: listOf(asset.name)
            (market.name in possibleNames || asset.name in (nameAliases[market.name] ?: listOf(market.name))) &&
                    market.account == asset.account &&
                    ((asset.quantity > 0 && market.type == "Покупка") || (asset.quantity < 0 && market.type == "Продажа"))
        }



        var remainingQuantity = kotlin.math.abs(asset.quantity)
        var totalCost = 0.0
        var totalQuantityUsed = 0

        for (entry in filteredEntries) {
            val quantityToUse = minOf(remainingQuantity, entry.quantity) // Берём только недостающее количество
            totalCost += quantityToUse * entry.price

            totalQuantityUsed += quantityToUse
            remainingQuantity -= quantityToUse

            if (remainingQuantity <= 0) break // Как только хватило количества, выходим из цикла
        }

        if (totalQuantityUsed > 0) {
            // Вычисляем среднюю цену и округляем до 2 знаков
            val averagePrice = BigDecimal(totalCost / totalQuantityUsed)
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()

            // Обновляем значение в базе данных
            db.execSQL(
                """
                UPDATE Assets
                SET price = ?
                WHERE name = ? AND account = ?
                """.trimIndent(),
                arrayOf(averagePrice, asset.name, asset.account)
            )
        }
    }
    db.close() // Закрываем базу данных
}

// рассортируем все цены покупки/продажи эмитентов по дате и времени
@Synchronized
fun DatabaseHelper.getSortedMainMarketEntries(): List<Markets> {
    val db = readableDatabase
    val markets = mutableListOf<Markets>()

    // Чтение данных из MainMarket
    val query = """
        SELECT date_conclusion, time, name, code, type, quantity, price, account
        FROM MainMarket
    """
    val cursor = db.rawQuery(query, null)
    cursor.use {
        if (it.moveToFirst()) {
            do {
                val date_conclusion = it.getString(it.getColumnIndexOrThrow("date_conclusion"))
                val time = it.getString(it.getColumnIndexOrThrow("time"))
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val code = it.getString(it.getColumnIndexOrThrow("code"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val quantity = it.getInt(it.getColumnIndexOrThrow("quantity"))
                val price = it.getDouble(it.getColumnIndexOrThrow("price"))
                val account = it.getString(it.getColumnIndexOrThrow("account"))

                markets.add(Markets(date_conclusion, time, name, code, type, quantity, price, account))
            } while (it.moveToNext())
        }
    }
    cursor.close()

    // Чтение данных из FuturesMarket
    val query2 = """
        SELECT date_conclusion, time, code_contract, type_contract, type, quantity, price, account
        FROM FuturesMarket
    """
    val cursor2 = db.rawQuery(query2, null)
    cursor2.use {
        if (it.moveToFirst()) {
            do {
                val date_conclusion = it.getString(it.getColumnIndexOrThrow("date_conclusion"))
                val time = it.getString(it.getColumnIndexOrThrow("time"))
                val code_contract = it.getString(it.getColumnIndexOrThrow("code_contract"))
                val type_contract = it.getString(it.getColumnIndexOrThrow("type_contract"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val quantity = it.getInt(it.getColumnIndexOrThrow("quantity"))
                val price = it.getDouble(it.getColumnIndexOrThrow("price"))
                val account = it.getString(it.getColumnIndexOrThrow("account"))

                markets.add(Markets(date_conclusion, time, code_contract, type_contract, type, quantity, price, account))
            } while (it.moveToNext())
        }
    }
    cursor2.close()

    // Чтение данных из REPOTransactions
    val query3 = """
        SELECT date_conclusion, time, name, code, type, quantity, price1, account
        FROM REPOTransactions
    """
    val cursor3 = db.rawQuery(query3, null)
    cursor3.use {
        if (it.moveToFirst()) {
            do {
                val date_conclusion = it.getString(it.getColumnIndexOrThrow("date_conclusion"))
                val time = it.getString(it.getColumnIndexOrThrow("time"))
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val code = it.getString(it.getColumnIndexOrThrow("code"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val quantity = it.getInt(it.getColumnIndexOrThrow("quantity"))
                val price1 = it.getDouble(it.getColumnIndexOrThrow("price1"))
                val account = it.getString(it.getColumnIndexOrThrow("account"))

                markets.add(Markets(date_conclusion, time, name, code, type, quantity, price1, account))
            } while (it.moveToNext())
        }
    }
    cursor3.close()

    // Чтение данных из CentralBankMove
    val query4 = """
        SELECT date_operation, date_buy, name, code, quantity, price, account
        FROM CentralBankMove
    """
    val cursor4 = db.rawQuery(query4, null)
    cursor4.use {
        if (it.moveToFirst()) {
            do {
                val date_conclusion = it.getString(it.getColumnIndexOrThrow("date_operation"))
                val time = it.getString(it.getColumnIndexOrThrow("date_buy")) // "00:00:00" //
                val name = it.getString(it.getColumnIndexOrThrow("name"))
                val code = it.getString(it.getColumnIndexOrThrow("code"))
                val type = "Покупка"
                val quantity = it.getInt(it.getColumnIndexOrThrow("quantity"))
                val price = it.getDouble(it.getColumnIndexOrThrow("price"))
                val account = it.getString(it.getColumnIndexOrThrow("account"))

                markets.add(Markets(date_conclusion, time, name, code, type, quantity, price, account))
            } while (it.moveToNext())
        }
    }
    cursor4.close()

    // Сортировка объединённых данных
//    return markets.sortedWith(compareBy({ it.date_conclusion }, { it.time }))
    return markets.sortedWith(compareByDescending<Markets> { it.date_conclusion }
        .thenByDescending { it.time })
}

// Копирование всех записей из таблицы Assets в класс CaseAll
@Synchronized
fun convertToCaseAll(assetsList: List<Assets>, categories: List<String>): List<CaseAll> {
    return assetsList
        .filter { asset ->
            categories.any { category ->
                asset.category.matches(Regex(category, RegexOption.IGNORE_CASE))
            }
        }
        .groupBy { it.name }
        .map { (name, groupedAssets) ->
            // Суммируем количество
            val totalQuantity = groupedAssets.sumOf { it.quantity }

            // Считаем средневзвешенную цену
            val totalCost = groupedAssets.sumOf { it.quantity * it.price }
            val averagePrice = if (totalQuantity != 0) {
                BigDecimal(totalCost / totalQuantity).setScale(2, RoundingMode.HALF_UP).toDouble()
            } else 0.0

            // Берём данные первого объекта для других полей
            val firstAsset = groupedAssets.first()
            CaseAll(
                name = name,
                isin = firstAsset.isin,
                ticker = firstAsset.ticker,
                currency = firstAsset.currency,
                quantity = totalQuantity,
                category = firstAsset.category,
                price = averagePrice,
                price_prev = firstAsset.price_prev,
                price_open = firstAsset.price_open,
                price_last = firstAsset.price_last,
                decimals = firstAsset.decimals,
                account = ""
            )
        }
}

// загружаем данные колонки category из таблицы Guide в таблицу Assets
@Synchronized
fun DatabaseHelper.updateCategoryAndTickerInAssets() {
    val db = writableDatabase
    db.beginTransaction()
    try {
        db.execSQL("""
            UPDATE Assets
            SET 
                category = (
                    SELECT Guide.category
                    FROM Guide
                    WHERE Guide.isin = Assets.isin
                ),
                ticker = (
                    SELECT Guide.code
                    FROM Guide
                    WHERE Guide.isin = Assets.isin
                )
            WHERE EXISTS (
                SELECT 1
                FROM Guide
                WHERE Guide.isin = Assets.isin
            )
        """)
        db.setTransactionSuccessful()
    } catch (e: Exception) {
        Log.e("DatabaseUpdate", "Ошибка при обновлении категорий и тикеров в таблице Assets: ${e.message}")
    } finally {
        db.endTransaction()
    }
}

// загружаем в виджет свободные средства портфеля
@Synchronized
fun DatabaseHelper.updateTextViewFreeCash(): String {
    val cash = getAllFreeCash() // данные из таблицы

    var RUB = BigDecimal.ZERO
    var USD = BigDecimal.ZERO
    var EUR = BigDecimal.ZERO
    var CNY = BigDecimal.ZERO
    for (i in 0 until cash.size) {
        RUB += BigDecimal(cash[i].RUB.trim()).setScale(2, RoundingMode.HALF_UP)
        USD += BigDecimal(cash[i].USD.trim()).setScale(2, RoundingMode.HALF_UP)
        EUR += BigDecimal(cash[i].EUR.trim()).setScale(2, RoundingMode.HALF_UP)
        CNY += BigDecimal(cash[i].CNY.trim()).setScale(2, RoundingMode.HALF_UP)
    }
    // Приведение итоговых значений к двум знакам после запятой
    RUB = RUB.setScale(2, RoundingMode.HALF_UP)
    USD = USD.setScale(2, RoundingMode.HALF_UP)
    EUR = EUR.setScale(2, RoundingMode.HALF_UP)
    CNY = CNY.setScale(2, RoundingMode.HALF_UP)
    // Если нужно, преобразуем BigDecimal обратно в Double
    val RUBResult = RUB.toDouble()
    val USDResult = USD.toDouble()
    val EURResult = EUR.toDouble()
    val CNYResult = CNY.toDouble()
    val displayText = listOfNotNull(
        if (RUBResult != 0.0) "${RUBResult} ₽" else "${RUBResult} ₽",
        if (USDResult != 0.0) "${USDResult} \$" else null,
        if (EURResult != 0.0) "${EURResult} €" else null,
        if (CNYResult != 0.0) "${CNYResult} ¥" else null
    ).joinToString(separator = "\n")
//    return displayText // выводит четыре валюты
    return RUBResult.toString() // выводит только рубли
}

// Получение всех записей из таблицы Assets
@Synchronized
fun DatabaseHelper.getAllAssets(): List<Assets> {
    val db = readableDatabase
    val assets = mutableListOf<Assets>()
//    val cursor = db.rawQuery("SELECT * FROM Assets", null)
    // Сортировка по имени
    val cursor = db.rawQuery("SELECT * FROM Assets ORDER BY name ASC", null)
    if (cursor.moveToFirst()) {
        do {
            assets.add(
                Assets(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    isin = cursor.getString(cursor.getColumnIndexOrThrow("isin")),
                    ticker = cursor.getString(cursor.getColumnIndexOrThrow("ticker")),
                    currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                    category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    price_prev = cursor.getDouble(cursor.getColumnIndexOrThrow("price_prev")),
                    price_open = cursor.getDouble(cursor.getColumnIndexOrThrow("price_open")),
                    price_last = cursor.getDouble(cursor.getColumnIndexOrThrow("price_last")),
                    decimals = cursor.getInt(cursor.getColumnIndexOrThrow("decimals")),
                    account = cursor.getString(cursor.getColumnIndexOrThrow("account"))
                )
            )
        } while (cursor.moveToNext())
    }
    cursor.close()
    return assets
}

// Получение всех записей из таблицы Payments
@Synchronized
fun DatabaseHelper.getAllPayments(): Double {
    val db = readableDatabase
    val payments = mutableListOf<Payments>()
    val cursor = db.rawQuery("SELECT * FROM Payments", null)
    if (cursor.moveToFirst()) {
        do {
            payments.add(
                Payments(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    marketType = cursor.getString(cursor.getColumnIndexOrThrow("marketType")),
                    operations = cursor.getString(cursor.getColumnIndexOrThrow("operations")),
                    currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
                    credit = cursor.getDouble(cursor.getColumnIndexOrThrow("credit")),
                    account = cursor.getString(cursor.getColumnIndexOrThrow("account"))
                )
            )
        } while (cursor.moveToNext())
    }
    cursor.close()

    var sum = BigDecimal.ZERO
    for (i in payments) {
        sum += BigDecimal(i.credit).setScale(2, RoundingMode.HALF_UP)
    }
    // Приведение итоговых значений к двум знакам после запятой
    sum = sum.setScale(2, RoundingMode.HALF_UP)
    // Если нужно, преобразуем BigDecimal обратно в Double
    val sumResult = sum.toDouble()
    return sumResult
}

// Получение всех записей из таблицы FreeCash
@Synchronized
fun DatabaseHelper.getAllFreeCash(): List<FreeCash> {
    val db = readableDatabase
    val cash = mutableListOf<FreeCash>()
    val cursor = db.rawQuery("SELECT * FROM FreeCash", null)
    if (cursor.moveToFirst()) {
        do {
            cash.add(
                FreeCash(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    RUB = cursor.getString(cursor.getColumnIndexOrThrow("RUB")),
                    USD = cursor.getString(cursor.getColumnIndexOrThrow("USD")),
                    EUR = cursor.getString(cursor.getColumnIndexOrThrow("EUR")),
                    CNY = cursor.getString(cursor.getColumnIndexOrThrow("CNY")),
                    marketType = cursor.getString(cursor.getColumnIndexOrThrow("marketType")),
                    account = cursor.getString(cursor.getColumnIndexOrThrow("account"))
                )
            )
        } while (cursor.moveToNext())
    }
    cursor.close()

//    Log.e("aaa", "cash: ==== $cash")
    return cash
}

// считаем по таблице CashFlow свободные средства и записываем в таблицу FreeCash
@Synchronized
fun DatabaseHelper.updateFreeCashFromCashFlow() {
    val db = writableDatabase
    db.beginTransaction()
    try {
        // Получаем суммы кредитов и дебетов по валютам, аккаунтам и типам рынков
        val cashFlowCursor = db.rawQuery("""
            SELECT 
                currency, 
                account,
                marketType,
                SUM(CAST(credit AS REAL)) AS totalCredit, 
                SUM(CAST(debit AS REAL)) AS totalDebit
            FROM CashFlow
            GROUP BY currency, account, marketType
        """, null)

        // Словарь для хранения данных
        val updatedValues = mutableMapOf<String, MutableMap<String, BigDecimal>>() // <account, <currency_market, amount>>
        cashFlowCursor.use {
            while (it.moveToNext()) {
                val currency = it.getString(it.getColumnIndexOrThrow("currency"))
                val account = it.getString(it.getColumnIndexOrThrow("account"))
                val marketType = it.getString(it.getColumnIndexOrThrow("marketType"))
                val totalCredit = it.getDouble(it.getColumnIndexOrThrow("totalCredit"))
                val totalDebit = it.getDouble(it.getColumnIndexOrThrow("totalDebit"))
                // Переводим в BigDecimal и округляем
                val credit = BigDecimal(totalCredit).setScale(2, RoundingMode.HALF_UP)
                val debit = BigDecimal(totalDebit).setScale(2, RoundingMode.HALF_UP)
                val netAmount = credit.subtract(debit).setScale(2, RoundingMode.HALF_UP)

//                if (marketType == "Фондовый рынок" && account == "40467CZ") {
                // Логируем данные
//                Log.e("aaa", "currency: $currency")
//                Log.e("aaa", "account: $account")
//                Log.e("aaa", "marketType: $marketType")
//                Log.e("aaa", "credit: $credit")
//                Log.e("aaa", "debit: $debit")
//                Log.e("aaa", "netAmount: $netAmount")
//                Log.e("aaa", "==========================")
//                }

                val accountMap = updatedValues.getOrPut(account) { mutableMapOf() }
                val key = "${currency}_$marketType"
                accountMap[key] = accountMap.getOrDefault(key, BigDecimal.ZERO).add(netAmount)
            }
        }

        // Обновляем таблицу FreeCash после округления итогов
        for ((account, currencyMap) in updatedValues) {
            for ((key, total) in currencyMap) {
                val roundedAmount = total.setScale(2, RoundingMode.HALF_UP)

                val (currency, marketType) = key.split("_")
                val column = when (currency) {
                    "RUB" -> "RUB"
                    "USD" -> "USD"
                    "EUR" -> "EUR"
                    "CNY" -> "CNY"
                    else -> null
                }
                if (column != null) {
                    // Проверяем, существует ли запись в FreeCash
                    val checkCursor = db.rawQuery("""
                        SELECT COUNT(*) FROM FreeCash WHERE account = ? AND marketType = ?
                    """, arrayOf(account, marketType))
                    var recordExists = false
                    checkCursor.use {
                        if (it.moveToFirst()) {
                            recordExists = it.getInt(0) > 0
                        }
                    }
                    checkCursor.close()
                    if (recordExists) {
                        // Если запись существует, обновляем её
                        db.execSQL("""
                            UPDATE FreeCash SET $column = ? 
                            WHERE account = ? AND marketType = ?
                        """, arrayOf(roundedAmount.toPlainString(), account, marketType))
                    } else {
                        // Если записи нет, добавляем новую
                        db.execSQL("""
                            INSERT INTO FreeCash (RUB, USD, EUR, CNY, marketType, account)
                            VALUES (?, ?, ?, ?, ?, ?)
                        """, arrayOf(
                            if (column == "RUB") roundedAmount.toPlainString() else "0",
                            if (column == "USD") roundedAmount.toPlainString() else "0",
                            if (column == "EUR") roundedAmount.toPlainString() else "0",
                            if (column == "CNY") roundedAmount.toPlainString() else "0",
                            marketType,
                            account
                        ))
                    }
                }
            }
        }
        db.setTransactionSuccessful()
    } catch (e: Exception) {
        Log.e("aaa", "Ошибка при обновлении таблицы FreeCash: ${e.message}")
    } finally {
        db.endTransaction()
    }
}

// определяем по аккаунту и дате новизну отчета брокера
@Synchronized
fun DatabaseHelper.isFileNew(fileName: String): Boolean {
    val db = readableDatabase
    // Функция для извлечения даты или самой поздней даты из имени файла
    fun extractLatestDate(filename: String): Date? {
        val dateRegex = Regex("""\d{6,8}""") // Ищем даты формата ddMMyy или ddMMyyyy
        val dates = dateRegex.findAll(filename).mapNotNull {
            try {
                SimpleDateFormat(if (it.value.length == 6) "ddMMyy" else "ddMMyyyy", Locale.getDefault()).parse(it.value)
            } catch (e: Exception) {
                null
            }
        }.toList()
        return dates.maxOrNull() // Возвращаем самую позднюю дату
    }
    // Функция для извлечения аккаунта из имени файла
    fun extractAccount(fileName: String): String {
        return fileName.substringBefore("_") // Извлекаем все символы до первого _
    }
    // Извлекаем самую позднюю дату и аккаунт из имени переданного файла
    val fileDate = extractLatestDate(fileName) ?: return false
    val account = extractAccount(fileName) ?: return false
    // Извлекаем все файлы из таблицы для данного аккаунта
    val accountFiles = mutableListOf<String>()
    val accountCursor = db.rawQuery("SELECT name FROM FilesHTML WHERE name LIKE ?", arrayOf("$account%"))
    while (accountCursor.moveToNext()) {
        accountFiles.add(accountCursor.getString(0))
    }
    accountCursor.close()
    // Определяем самую свежую дату среди файлов в таблице
    val latestDateInDb = accountFiles
        .mapNotNull { extractLatestDate(it) }
        .maxOrNull()
    // Сравниваем даты
    return latestDateInDb == null || fileDate > latestDateInDb
}

// Получение всех записей из таблицы FilesHTML
@Synchronized
fun DatabaseHelper.getFilesHTML(): List<FilesHTML> {
    val db = readableDatabase
    val files = mutableListOf<FilesHTML>()
    val cursor = db.rawQuery("SELECT * FROM FilesHTML", null)
    if (cursor.moveToFirst()) {
        do {
            files.add(
                FilesHTML(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                )
            )
        } while (cursor.moveToNext())
    }
    cursor.close()
    return files
}

// удаляет строки из таблицы на основе заданного условия (WHERE account = ?)
@Synchronized // доступ к базе данных синхронизирован
fun DatabaseHelper.deleteTableByAccount(name: String, account: String) {
    val db = writableDatabase
    db.delete(name, "account = ?", arrayOf(account))
}


