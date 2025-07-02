//package com.example.investmentmonitor
//
//import android.content.Context
//import android.util.Log
//import org.apache.poi.ss.usermodel.CellType
//import org.apache.poi.ss.usermodel.DateUtil
//import org.apache.poi.ss.usermodel.WorkbookFactory
//import java.io.InputStream
//import java.text.SimpleDateFormat
//
//class ReadExcelFile {}
//
//// Класс для хранения данных о ячейке
//data class CellData(val columnIndex: Int, val value: String)
//
//// Класс для хранения данных о строке
//data class RowData(val rowIndex: Int, val cells: List<CellData>)
//
//// Класс для хранения данных о листе
//data class SheetData(val sheetName: String, val rows: List<RowData>)
//
//// Функция для чтения Excel-файла и создания структуры данных
//fun readExcelFile(context: Context, fileName: String): List<SheetData> {
//    // Открываем поток для чтения Excel-файла из ресурсов приложения
//    val inputStream: InputStream = context.assets.open(fileName)
//
//    // Создаем объект Workbook из входного потока
//    val workbook = WorkbookFactory.create(inputStream)
//
//    // Список для хранения данных всех листов
//    val sheets = mutableListOf<SheetData>()
//
//    // Создаем объект SimpleDateFormat для форматирования дат
//    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//
//    // Перебираем все листы в рабочей книге
//    for (sheetIndex in 0 until workbook.numberOfSheets) {
//        // Получаем текущий лист по индексу
//        val sheet = workbook.getSheetAt(sheetIndex)
//
//        // Список для хранения данных всех строк на текущем листе
//        val rows = mutableListOf<RowData>()
//
//        // Перебираем все строки на текущем листе
//        for (rowIndex in sheet.firstRowNum..sheet.lastRowNum) {
//            // Получаем строку по индексу
//            val row = sheet.getRow(rowIndex)
//
//            // Список для хранения данных всех ячеек в текущей строке
//            val cells = mutableListOf<CellData>()
//
//            // Перебираем все ячейки в текущей строке
//            row?.let {
//                for (cellIndex in it.firstCellNum until it.lastCellNum) {
//                    // Получаем ячейку по индексу
//                    val cell = it.getCell(cellIndex)
//
//                    // Определяем тип ячейки и извлекаем значение
//                    val cellValue: String = when (cell?.cellType) {
//                        // Если ячейка содержит строку
//                        CellType.STRING -> cell.stringCellValue
//                        // Если ячейка содержит число
//                        CellType.NUMERIC -> {
//                            if (DateUtil.isCellDateFormatted(cell)) {
//                                // Если ячейка содержит дату, форматируем ее
//                                val date = cell.dateCellValue
//                                dateFormat.format(date)
//                            } else {
//                                // Иначе выводим число как строку
//                                cell.numericCellValue.toString()
//                            }
//                        }
//                        // Если ячейка содержит булевое значение
//                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
//                        // Для всех остальных типов
//                        else -> "UNKNOWN"
//                    }
//
//                    // Добавляем данные о ячейке в список ячеек
//                    cells.add(CellData(cellIndex, cellValue))
//                }
//                // Добавляем данные о строке в список строк
//                rows.add(RowData(rowIndex, cells))
//            }
//        }
//
//        // Добавляем данные о листе в список листов
//        sheets.add(SheetData(sheet.sheetName, rows))
//    }
//
//    // Закрываем рабочую книгу
//    workbook.close()
//
//    // Возвращаем список всех листов с данными
//    return sheets
//}
//
//// Функция для вывода данных о листах, строках и ячейках
//fun printSheetData(sheetData: List<SheetData>) {
////    val tag = "aaa" // Тэг для логов
//
//    for ((sheetIndex, sheet) in sheetData.withIndex()) {
////        Log.e(tag, "---Sheet ${sheetIndex}: ${sheet.sheetName}")
//        for (row in sheet.rows) {
////            Log.e(tag, "---Row ${row.rowIndex}:")
//            for (cell in row.cells) {
////                Log.e(tag, "---Column ${cell.columnIndex}: ${cell.value}")
//            }
////            Log.e(tag, "---------------")
//        }
////        Log.e(tag, "-------------------------------------")
//    }
//}
////Log.e(tag, "-----------${stringBuilder}")
//
////fun readExcelFile(context: Context, fileName: String) {
////    val inputStream: InputStream = context.assets.open(fileName)
////    val workbook = WorkbookFactory.create(inputStream)
////    val tag = "aaa" // Тэг для логов
////
////    // Перебор всех листов в книге
////    for (sheetIndex in 0 until workbook.numberOfSheets) {
////        val sheet: Sheet = workbook.getSheetAt(sheetIndex)
////        Log.d(tag, "---Sheet Name: ${sheet.sheetName}") // Логируем имя листа
////
////        // Перебор всех строк на листе
////        for (row: Row in sheet) {
////            val stringBuilder = StringBuilder()
////
////            // Перебор всех ячеек в строке
////            for (cell in row) {
////                when (cell.cellType) {
////                    CellType.STRING -> {
////                        stringBuilder.append("${cell.stringCellValue}\t")
////                    }
////                    CellType.NUMERIC -> {
////                        if (DateUtil.isCellDateFormatted(cell)) {
////                            val date = cell.dateCellValue
////                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Форматирование даты и времени
////                            stringBuilder.append("${dateFormat.format(date)}\t")
////                        } else {
////                            stringBuilder.append("${cell.numericCellValue}\t")
////                        }
////                    }
////                    CellType.BOOLEAN -> {
////                        stringBuilder.append("${cell.booleanCellValue}\t")
////                    }
////                    else -> {
////                        stringBuilder.append("UNKNOWN\t")
////                    }
////                }
////            }
////
////            // Выводим строку в лог
////            Log.e(tag, "-----------${stringBuilder}")
////        }
////    }
////
////    workbook.close()
////}
//
////fun readExcelFile(context: Context, fileName: String) {
////    val inputStream: InputStream = context.assets.open(fileName)
////    val workbook = WorkbookFactory.create(inputStream)
////    val sheet: Sheet = workbook.getSheetAt(0)  // Получаем первый лист
////
////    val tag = "ExcelReader" // Можно задать любой тэг для логов
////
////    for (row: Row in sheet) {
////        val stringBuilder = StringBuilder()
////
////        for (cell in row) {
////            when (cell.cellType) {
////                org.apache.poi.ss.usermodel.CellType.STRING -> {
////                    stringBuilder.append("${cell.stringCellValue}\t")
////                }
////                org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
////                    stringBuilder.append("${cell.numericCellValue}\t")
////                }
////                org.apache.poi.ss.usermodel.CellType.BOOLEAN -> {
////                    stringBuilder.append("${cell.booleanCellValue}\t")
////                }
////                else -> {
////                    stringBuilder.append("UNKNOWN\t")
////                }
////            }
////        }
////
////        // Выводим строку в лог
////        Log.d("aaa", stringBuilder.toString())
////    }
////
////    workbook.close()
////}