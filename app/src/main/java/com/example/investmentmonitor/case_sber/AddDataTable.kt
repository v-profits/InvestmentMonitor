package com.example.investmentmonitor.case_sber

import android.content.ContentValues

// Вставка записи в таблицу "Assets"
fun DatabaseHelper.addAssets(
    name: String,
    isin: String,
    ticker: String,
    currency: String,
    quantity: Int,
    category: String,
    price: Double,

    price_prev: Double,
    price_open: Double,
    price_last: Double,
    decimals: Int,

    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("name", name)
        put("isin", isin)
        put("ticker", ticker)
        put("currency", currency)
        put("quantity", quantity)
        put("category", category)
        put("price", price)

        put("price_prev", price_prev)
        put("price_open", price_open)
        put("price_last", price_last)
        put("decimals", decimals)

        put("account", account)
    }
    val result = db.insert("Assets",null,values)
//    Log.e("aaa","result: ${result}")
    return result
}

// Вставка записи в таблицу "Stocks"
fun DatabaseHelper.addStocks(
    name: String,
    isin: String,
    currency: String,
    quantity: Int,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("name", name)
        put("isin", isin)
        put("currency", currency)
        put("quantity", quantity)
        put("account", account)
    }
    val result = db.insert("Stocks",null,values)
    return result
}

// Вставка записи в таблицу "Contracts"
fun DatabaseHelper.addContracts(
    code: String,
    type: String,
    quantity: Int,
    date: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("code", code)
        put("type", type)
        put("quantity", quantity)
        put("date", date)
        put("account", account)
    }
    val result = db.insert("Contracts",null,values)
    return result
}

// Вставка записи в таблицу "FilesHTML"
fun DatabaseHelper.addFilesHTML(
    name: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("name", name)
    }
    val result = db.insert("FilesHTML",null,values)
    return result
}

// Вставка записи в таблицу "MainMarket"
fun DatabaseHelper.addMainMarket(
    date_conclusion: String,
    date_settlement: String,
    time: String,
    name: String,
    code: String,
    currency: String,
    type: String,
    quantity: Int,
    price: Double,
    total: Double,
    nkd: Double,
    broker_commission: Double,
    stock_market_commission: Double,
    transaction_number: String,
    comment: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date_conclusion", date_conclusion)
        put("date_settlement", date_settlement)
        put("time", time)
        put("name", name)
        put("code", code)
        put("currency", currency)
        put("type", type)
        put("quantity", quantity)
        put("price", price)
        put("total", total)
        put("nkd", nkd)
        put("broker_commission", broker_commission)
        put("stock_market_commission", stock_market_commission)
        put("transaction_number", transaction_number)
        put("comment", comment)
        put("account", account)
    }
    val result = db.insert("MainMarket",null,values)
    return result
}

// Вставка записи в таблицу "FuturesMarket"
fun DatabaseHelper.addFuturesMarket(
    date_conclusion: String,
    date_settlement: String,
    time: String,
    code_contract: String,
    type_contract: String,
    type: String,
    quantity: Int,
    price: Double,
    broker_commission: Double,
    stock_market_commission: Double,
    transaction_number: String,
    comment: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date_conclusion", date_conclusion)
        put("date_settlement", date_settlement)
        put("time", time)
        put("code_contract", code_contract)
        put("type_contract", type_contract)
        put("type", type)
        put("quantity", quantity)
        put("price", price)
        put("broker_commission", broker_commission)
        put("stock_market_commission", stock_market_commission)
        put("transaction_number", transaction_number)
        put("comment", comment)
        put("account", account)
    }
    val result = db.insert("FuturesMarket",null,values)
    return result
}

// Вставка записи в таблицу "REPOTransactions"
fun DatabaseHelper.addREPOTransactions(
    date_conclusion: String,
    time: String,
    name: String,
    code: String,
    currency: String,
    type: String,
    quantity: Int,
    price1: Double,
    nkd1: Double,
    total1: Double,
    date_settlement1: String,
    REPO_rate: Double,
    REPO_percentage: Double,
    price2: Double,
    nkd2: Double,
    total2: Double,
    date_settlement2: String,
    quantity_central_banks: Int,
    total_margins: Double,
    broker_commission: Double,
    stock_market_commission: Double,
    transaction_number: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date_conclusion", date_conclusion)
        put("time", time)
        put("name", name)
        put("code", code)
        put("currency", currency)
        put("type", type)
        put("quantity", quantity)
        put("price1", price1)
        put("nkd1", nkd1)
        put("total1", total1)
        put("date_settlement1", date_settlement1)
        put("REPO_rate", REPO_rate)
        put("REPO_percentage", REPO_percentage)
        put("price2", price2)
        put("nkd2", nkd2)
        put("total2", total2)
        put("date_settlement2", date_settlement2)
        put("quantity_central_banks", quantity_central_banks)
        put("total_margins", total_margins)
        put("broker_commission", broker_commission)
        put("stock_market_commission", stock_market_commission)
        put("transaction_number", transaction_number)
        put("account", account)
    }
    val result = db.insert("REPOTransactions",null,values)
    return result
}

// Вставка записи в таблицу "CentralBankMove"
fun DatabaseHelper.addCentralBankMove(
    date_operation: String,
    name: String,
    code: String,
    type: String,
    operations: String,
    quantity: Int,
    date_buy: String,
    price: Double,
    report_id: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date_operation", date_operation)
        put("name", name)
        put("code", code)
        put("type", type)
        put("operations", operations)
        put("quantity", quantity)
        put("date_buy", date_buy)
        put("price", price)
        put("report_id", report_id)
        put("account", account)
    }
    val result = db.insert("CentralBankMove",null,values)
    return result
}

// Вставка записи в таблицу "CashFlow"
fun DatabaseHelper.addCashFlow(
    date: String,
    marketType: String,
    operations: String,
    currency: String,
    credit: Double,
    debit: Double,
    report_id: String,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date", date)
        put("marketType", marketType)
        put("operations", operations)
        put("currency", currency)
        put("credit", credit)
        put("debit", debit)
        put("report_id", report_id)
        put("account", account)
    }
    val result = db.insert("CashFlow",null,values)
    return result
}

fun DatabaseHelper.addPayments(
    date: String,
    marketType: String,
    operations: String,
    currency: String,
    credit: Double,
    account: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("date", date)
        put("marketType", marketType)
        put("operations", operations)
        put("currency", currency)
        put("credit", credit)
        put("account", account)
    }
    val result = db.insert("Payments",null,values)
    return result
}

// Вставка записи в таблицу "Guide"
fun DatabaseHelper.addGuide(
    name: String,
    code: String,
    isin: String,
    issuer: String,
    category: String,
    series: String
): Long {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put("name", name)
        put("code", code)
        put("isin", isin)
        put("issuer", issuer)
        put("category", category)
        put("series", series)
    }
    val result = db.insert("Guide",null,values)
    return result
}

