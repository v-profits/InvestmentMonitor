package com.example.investmentmonitor.MOEX

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(val context: Context) {

//    // Функция для сохранения данных
//    // данные списков сериализуются в JSON с помощью Gson и сохраняются в SharedPreferences
//    fun saveTickerStocksToPrefs(context: Context) {
//        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//
//        val gson = Gson()
//
//        // Сериализация объектов
//        val backupTickerCaseJson = gson.toJson(backupTickerCase)
//        // Сохранение JSON строк в SharedPreferences
//        editor.putString("Case", backupTickerCaseJson)
//        editor.apply()
//
//        // Futures
//        val backupTickerFuturesJson = gson.toJson(backupTickerFutures)
//        editor.putString("Futures", backupTickerFuturesJson)
//        editor.apply()
//        // Stock
//        val backupTickerStocksJson = gson.toJson(backupTickerStock)
//        editor.putString("TQBR", backupTickerStocksJson)
//        editor.apply()
//        // Fund
//        val backupTickerFundsJson = gson.toJson(backupTickerFund)
//        editor.putString("TQTF", backupTickerFundsJson)
//        editor.apply()
//        // Bond
//        val backupTickerBondsJson = gson.toJson(backupTickerBond)
//        editor.putString("TQOB", backupTickerBondsJson)
//        editor.apply()
//    }
//
//    // Функция для выгрузки данных
//    // JSON строки десериализуются обратно в объекты с помощью Gson
//    fun loadTickerStocksFromPrefs(context: Context) {
//
//        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//
//        // Получение JSON строк из SharedPreferences
//        val backupTickerCaseJson = sharedPreferences.getString("Case", null)
//        // Десериализация объектов
//        if (backupTickerCaseJson != null) {
//            val gson = Gson()
//            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
//            backupTickerCase = gson.fromJson(backupTickerCaseJson, type)
//        }
//        // Futures
//        val backupTickerFuturesJson = sharedPreferences.getString("Futures", null)
//        if (backupTickerFuturesJson != null) {
//            val gson = Gson()
//            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
//            backupTickerFutures = gson.fromJson(backupTickerFuturesJson, type)
//        }
//        // Stock
//        val backupTickerStocksJson = sharedPreferences.getString("TQBR", null)
//        // Десериализация объектов
//        if (backupTickerStocksJson != null) {
//            val gson = Gson()
//            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
//            backupTickerStock = gson.fromJson(backupTickerStocksJson, type)
//        }
//        // Fund
//        val backupTickerFundsJson = sharedPreferences.getString("TQTF", null)
//        // Десериализация объектов
//        if (backupTickerFundsJson != null) {
//            val gson = Gson()
//            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
//            backupTickerFund = gson.fromJson(backupTickerFundsJson, type)
//        }
//        // Bond
//        val backupTickerBondsJson = sharedPreferences.getString("TQOB", null)
//        // Десериализация объектов
//        if (backupTickerBondsJson != null) {
//            val gson = Gson()
//            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
//            backupTickerBond = gson.fromJson(backupTickerBondsJson, type)
//        }
//    }



    //--- для вызова или сохранения из фрагмента ===================================================

////////////////////////////////////////////////////////////////////////////////////////////////////
// Сохранение данных
//    PreferencesManager.getInstance(this).saveBoolean("isGame", data.isGame)
// Выгружаем данные
//    val levelsTwoList = PreferencesManager.getInstance(activity).getTwoListInt("levelsTwoList")
////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    //--- TickerResponse
    fun setTickerResponse(key: String, value: MutableList<TickerResponse> ) {
        // Получение SharedPreferences
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Сериализация объектов
        val gson = Gson()
        val backupTickerJson = gson.toJson(value)
        // Сохранение JSON строк в SharedPreferences
        editor.putString(key, backupTickerJson)
        editor.apply()
    }
    fun getTickerResponse(key: String): MutableList<TickerResponse> {
        // Получение SharedPreferences
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Получение JSON строк из SharedPreferences
        val backupTickerJson = sharedPreferences.getString(key, null)
        // Десериализация объектов
        var value = mutableListOf<TickerResponse>()
        if (backupTickerJson != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<TickerResponse>>() {}.type
            value = gson.fromJson(backupTickerJson, type)
        }
        return value
    }
}