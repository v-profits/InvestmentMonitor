package com.example.investmentmonitor.case_sber

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// База данных
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // Таблица "Assets" // активы портфеля - выборка по другим таблицам
        db.execSQL(
            """
            CREATE TABLE Assets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                isin TEXT,
                ticker TEXT,
                currency TEXT,
                quantity INTEGER,
                category TEXT,
                price REAL,
                price_prev REAL,
                price_open REAL,
                price_last REAL,
                decimals INTEGER,
                account TEXT
            )
        """
        )

        // Таблица "Stocks" // акции, облигации, фонды
        db.execSQL("""
            CREATE TABLE Stocks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                isin TEXT,
                currency TEXT,
                quantity INTEGER,
                account TEXT
            )
        """)

        // Таблица "Contracts" // фьючерсы
        db.execSQL("""
            CREATE TABLE Contracts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                code TEXT,
                type TEXT,
                quantity INTEGER,
                date TEXT,
                account TEXT
            )
        """)

        // Таблица "FilesHTML" // имена файлов брокера
        db.execSQL("""
            CREATE TABLE FilesHTML (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT
            )
        """)

        // Таблица "MainMarket" // "Сделки купли/продажи ценных бумаг"
        db.execSQL("""
            CREATE TABLE MainMarket (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date_conclusion TEXT, 
                date_settlement TEXT, 
                time TEXT,
                name TEXT,
                code TEXT,
                currency TEXT,
                type TEXT,
                quantity INTEGER,
                price REAL,
                total REAL,
                nkd REAL,
                broker_commission REAL,
                stock_market_commission REAL,
                transaction_number TEXT,
                comment TEXT,
                account TEXT
            )
        """)

        // Таблица "FuturesMarket" // "Срочные сделки"
        db.execSQL("""
            CREATE TABLE FuturesMarket (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date_conclusion TEXT, 
                date_settlement TEXT, 
                time TEXT,
                code_contract TEXT,
                type_contract TEXT,
                type TEXT,
                quantity INTEGER,
                price REAL,
                broker_commission REAL,
                stock_market_commission REAL,
                transaction_number INTEGER,
                comment TEXT,
                account TEXT
            )
        """)

        // Таблица "REPOTransactions" // "Сделки РЕПО"
        db.execSQL("""
            CREATE TABLE REPOTransactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date_conclusion TEXT,
                time TEXT,
                name TEXT,
                code TEXT,
                currency TEXT,
                type TEXT,
                quantity INTEGER,
                price1 REAL,
                nkd1 REAL,
                total1 REAL,
                date_settlement1 TEXT,
                REPO_rate REAL,
                REPO_percentage REAL,
                price2 REAL,
                nkd2 REAL,
                total2 REAL,
                date_settlement2 TEXT, 
                quantity_central_banks INTEGER,
                total_margins REAL,
                broker_commission REAL,
                stock_market_commission REAL,
                transaction_number INTEGER,
                account TEXT
            )
        """)

        // Таблица "CentralBankMove" // "Движение ЦБ, не связанное с исполнением сделок"
        db.execSQL("""
            CREATE TABLE CentralBankMove (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date_operation TEXT,
                name TEXT,
                code TEXT,
                type TEXT,
                operations TEXT,
                quantity INTEGER,
                date_buy TEXT,
                price REAL,
                report_id TEXT,
                account TEXT
            )
        """)

        // Таблица "CashFlow" // "Движение денежных средств за период"
        db.execSQL("""
            CREATE TABLE CashFlow (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                marketType TEXT,
                operations TEXT,
                currency TEXT,
                credit REAL,
                debit REAL,
                report_id TEXT,
                account TEXT
            )
        """)

        // Таблица "Payments" // "Выплаты дохода от эмитента на внешний счет"
        db.execSQL("""
            CREATE TABLE Payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                marketType TEXT,
                operations TEXT,
                currency TEXT,
                credit REAL,
                account TEXT
            )
        """)

        // Таблица "FreeCash" // свободные денежные средства
        db.execSQL("""
            CREATE TABLE FreeCash (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                RUB TEXT,
                USD TEXT,
                EUR TEXT,
                CNY TEXT,
                marketType TEXT,
                account TEXT
            )
        """)

        // Таблица "Guide" // справочник эмитентов портфеля
        db.execSQL("""
            CREATE TABLE Guide (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                code  TEXT,
                isin TEXT,
                issuer TEXT,
                category TEXT,
                series TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Assets") // мой портфель
        db.execSQL("DROP TABLE IF EXISTS Stocks") // акции, облигации и паи
        db.execSQL("DROP TABLE IF EXISTS Contracts") // фьючерсы и опционы
        db.execSQL("DROP TABLE IF EXISTS FilesHTML") // список отчетов брокера
        db.execSQL("DROP TABLE IF EXISTS MainMarket") // фондовый рынок
        db.execSQL("DROP TABLE IF EXISTS FuturesMarket") // срочный рынок
        db.execSQL("DROP TABLE IF EXISTS REPOTransactions") // рынок займов
        db.execSQL("DROP TABLE IF EXISTS CentralBankMove") // движение ЦБ
        db.execSQL("DROP TABLE IF EXISTS CashFlow") // денежный поток
        db.execSQL("DROP TABLE IF EXISTS Payments") // Выплаты дохода от эмитента на внешний счет
        db.execSQL("DROP TABLE IF EXISTS FreeCash") // свободные деньги
        db.execSQL("DROP TABLE IF EXISTS Guide") // справочник активов портфеля
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "BrokerReports.db"
        const val DATABASE_VERSION = 49
    }
}
