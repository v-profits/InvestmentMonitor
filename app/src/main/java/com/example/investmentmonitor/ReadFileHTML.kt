package com.example.investmentmonitor

import android.Manifest

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

private val REQUEST_CODE = 100 // Код для идентификации запроса разрешений
private const val NAME_PROJECT = "" // Указать имя вашего проекта


// Класс для хранения данных о ячейке (теги и их текст)
//data class CellData(val tagName: String, val value: String)
data class CellData(val value: String)

// Класс для хранения данных о строке (содержит список ячеек)
//data class RowData(val rowIndex: Int, val cells: List<CellData>)
data class RowData(val cells: List<CellData>)

// Класс для хранения данных о таблице
//data class TableData(val tableIndex: Int, val tableName: String, val rows: List<RowData>)
data class TableData(val rows: List<RowData>)

// Создание папки в "Загрузках"
fun createDownloadsFolder(context: Context): File? {
    // Получаем каталог "Загрузки"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    // Создаём новую папку для приложения
    val appFolder = File(downloadsDir, "InvestmentMonitor/Sber")
    if (!appFolder.exists()) {
        val created = appFolder.mkdir()
        if (created) {
            println("Папка создана: ${appFolder.absolutePath}")
        } else {
            println("Не удалось создать папку")
            return null
        }
    }
    return appFolder
}

// список моих существующих общедоступных файлов
fun listTextFiles(context: Context): List<String> {
    val fileList = mutableListOf<String>()

    // Проверяем разрешения
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            return fileList // Выходим из функции, пока разрешение не будет предоставлено
        }
    } else {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
            return fileList // Выходим из функции, пока разрешение не будет предоставлено
        }
    }

    val downloadsDir = File("/storage/emulated/0/Download/InvestmentMonitor/Sber")  // путь для эмулятора

    // считываем имена файлов брокера
    if (downloadsDir.exists() && downloadsDir.isDirectory) {
        val files = downloadsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".html")
        }
        files?.forEach { file ->
            fileList.add(file.name)
        }
    } else {
        Log.e("aaa", "Каталог не существует: /storage/emulated/0/Download")
    }

    if (fileList.isEmpty()) {
        Log.e("aaa", "No files found in Downloads")
    } else {
        Log.e("aaa", "Found files: $fileList")
    }

    return fileList // передаем список с именами файлов брокера
}


// Функция для парсинга HTML-файла и извлечения данных
fun parseHTMLFile(filePath: String): List<Any> {
    val file = File(filePath)
    if (!file.exists()) {
        Log.e("aaa", "File not found: $filePath")
        return emptyList()
    }

    val document: Document = Jsoup.parse(file, "UTF-8")
    val result = mutableListOf<Any>() // Список для хранения заголовков, текста и таблиц

    // Ищем все элементы <p><br>...</br></p> и таблицы в порядке следования
    val elements = document.body().children() // Берем непосредственных детей <body>

    elements.forEach { element ->
        when {
            // Если это текст или заголовок в <p><br>...</br></p>
            element.tagName() == "p" && element.select("br").isNotEmpty() -> {
                val text = element.text().trim()
                if (text.isNotEmpty()) {
                    result.add(text) // Добавляем текст в общий результат
                }
            }

            // Если это таблица
            element.tagName() == "table" -> {
                val rows = mutableListOf<RowData>()
                val rowElements = element.select("tr")

                // Перебираем строки таблицы
                rowElements.forEachIndexed { rowIndex, row ->
                    val cells = mutableListOf<CellData>()
                    val cellElements = row.select("td, th")

                    // Перебираем ячейки в строке
                    cellElements.forEach { cell ->
                        val value = cell.text().trim() // Текст внутри ячейки
                        cells.add(CellData(value))
                    }
                    rows.add(RowData(cells))
                }

                // Добавляем таблицу в общий результат
                result.add(TableData(rows))
            }
        }
    }

    return result
}

// Функция для вывода данных
fun printMixedData(data: List<Any>) {
    data.forEachIndexed  { index, element ->
//        if (element == "III. ИТОГОВЫЙ ФИНАНСОВЫЙ РЕЗУЛЬТАТ на 31.01.2024")
        if (element is String && element.contains("ИТОГОВЫЙ ФИНАНСОВЫЙ РЕЗУЛЬТАТ"))
        when (element) {
            is String -> { // Если элемент текст
                Log.e("aaa", "#$index Text: $element")
            }
            is TableData -> { // Если элемент таблица
                Log.e("aaa", "   #$index Table:")
                element.rows.forEachIndexed { index2, row ->
                    Log.e("aaa", "      #$index2 Row:")
                    row.cells.forEachIndexed { index3, cell ->
                        Log.e("aaa", "         #$index3 Cell: ${cell.value}")
                    }
                }
            }
            else -> {
                Log.e("aaa", "Unknown element: $element")
            }
        }
    }
}

// Функция для парсинга HTML-файла и извлечения имен таблиц
fun parseHTMLFileNames(filePath: String): List<String> {
    val file = File(filePath)
    if (!file.exists()) {
        Log.e("aaa","File not found: $filePath")
        return emptyList()
    }

    // Чтение HTML-файла
    val document: Document = Jsoup.parse(file, "UTF-8")
    val tableNames = mutableListOf<String>()

    // Находим все элементы <p> с <br> внутри
    val tableNameElements = document.select("p br")

    tableNameElements.forEach { brElement ->
        // Ищем родительский тег <p> и извлекаем текст
        val parentP = brElement.parent() // Получаем родительский элемент <p>
        val tableName = parentP?.text() // Извлекаем текст внутри <p>
        if (tableName != null) {
            if (tableName.isNotBlank()) {
                tableNames.add(tableName.trim()) // Добавляем имя таблицы в список
            }
        }
    }
    return tableNames
}

// функция для вывода имен таблиц
fun printTableNames(tableNames: List<String>) {
    tableNames.forEach { name ->
        Log.e("aaa", "Table Name: $name")
    }
}

//////////////////////////////////////////////////////////////
