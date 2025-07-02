package com.example.investmentmonitor.MOEX

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// преобразуем дату в период до закрытия бондов
fun bondsPeriod(date: String): String {
    val targetDate = try {
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        // Если формат даты неверный, возвращаем пустую строку или сообщение об ошибке
        return ""
    }
    val currentDate = LocalDate.now()

    val period = Period.between(currentDate, targetDate)

    val years = period.years
    val months = period.months

    val litter = when {
        years in 1..4 -> "г"
        else -> "л"
    }

    return "${years}${litter} ${months}мес" // Вывод: "20л 11м" (пример)
}

// отформатировать строковое число, чтобы оно выглядело как "23 456 789.1234"
fun formatNumber(input: String): String {
    if (input == "N/A") return input

    // Разделяем целую и дробную части
    val parts = input.split(".")
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) "." + parts[1] else ""

    // Форматируем целую часть с пробелами
    val formattedIntegerPart = integerPart.reversed().chunked(3).joinToString(" ").reversed()

    // Объединяем целую и дробную части
    return formattedIntegerPart + decimalPart
}

// сбрасываем флаг выбранного тиккера
fun resetBoards() {
    for ((name, value) in flagsMap) {
        if (value) flagsMap[name] = false
//                println("$name: $value")
//                if (name.contains("loadTQ")) {
//                    flagsMap[name] = true
//                }
    }
}

// отмечаем флаг выбранного тиккера
fun trueBoard(str: String) {
    for ((name, value) in flagsMap)
        if (str == name) flagsMap[name] = true
}

// получаем имя тиккера выбранного флага
fun nameBoard(): String {
    for ((name, value) in flagsMap)
        if (value) return name
    return ""
}



