package com.example.investmentmonitor

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.example.investmentmonitor.databinding.ActivityMainBinding
import com.example.investmentmonitor.MOEX.FragmentTicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import com.example.investmentmonitor.case_sber.CaseAll
import com.example.investmentmonitor.case_sber.DatabaseHelper
import com.example.investmentmonitor.MOEX.boardList
import com.example.investmentmonitor.MOEX.isMarketData
import com.example.investmentmonitor.MOEX.isSecurities
import com.example.investmentmonitor.MOEX.nameFragment
import com.example.investmentmonitor.MOEX.resetBoards
import com.example.investmentmonitor.MOEX.trueBoard
import com.example.investmentmonitor.MOEX.tv_title
import com.example.investmentmonitor.case_sber.FragmentCase
import com.example.investmentmonitor.case_sber.ViewModel


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 100 // Код для идентификации запроса разрешений

    // База данных
    private lateinit var dbHelper: DatabaseHelper

    private var job: Job? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeItem: FrameLayout
    private lateinit var caseItem: FrameLayout
    private lateinit var stockItem: FrameLayout
    private lateinit var fundItem: FrameLayout
    private lateinit var bondItem: FrameLayout
    private lateinit var indexItem: FrameLayout
    private lateinit var futuresItem: FrameLayout

    private lateinit var menuHomeText: TextView
    private lateinit var menuHomeIcon: ImageView
    private lateinit var menuCaseText: TextView
    private lateinit var menuCaseIcon: ImageView
    private lateinit var menuStockText: TextView
    private lateinit var menuStockIcon: ImageView
    private lateinit var menuFundText: TextView
    private lateinit var menuFundIcon: ImageView
    private lateinit var menuBondText: TextView
    private lateinit var menuBondIcon: ImageView
    private lateinit var menuIndexText: TextView
    private lateinit var menuIndexIcon: ImageView
    private lateinit var menuFuturesText: TextView
    private lateinit var menuFuturesIcon: ImageView

    private lateinit var viewModel: ViewModel

    private var colorNew = Color.GRAY
    private var colorOld = Color.GRAY

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge() // делает топ в цвет фона такие как статусная строка, панель навигации
        setContentView(R.layout.activity_main)
        //--- для корректного учета системных областей, таких как статусная строка, панель навигации и вырез экрана
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // виджеты итемов меню
        homeItem = findViewById(R.id.home_item)
        caseItem = findViewById(R.id.case_item)
        stockItem = findViewById(R.id.stock_item)
        fundItem = findViewById(R.id.fund_item)
        bondItem = findViewById(R.id.bond_item)
        indexItem = findViewById(R.id.index_item)
        futuresItem = findViewById(R.id.futures_item)

        // виджеты элементов итемов меню
        menuHomeText = findViewById(R.id.home_item_text)
        menuHomeIcon = findViewById(R.id.home_item_icon)
        menuCaseText = findViewById(R.id.case_item_text)
        menuCaseIcon = findViewById(R.id.case_item_icon)
        menuStockText = findViewById(R.id.stock_item_text)
        menuStockIcon = findViewById(R.id.stock_item_icon)
        menuFundText = findViewById(R.id.fund_item_text)
        menuFundIcon = findViewById(R.id.fund_item_icon)
        menuBondText = findViewById(R.id.bond_item_text)
        menuBondIcon = findViewById(R.id.bond_item_icon)
        menuIndexText = findViewById(R.id.index_item_text)
        menuIndexIcon = findViewById(R.id.index_item_icon)
        menuFuturesText = findViewById(R.id.futures_item_text)
        menuFuturesIcon = findViewById(R.id.futures_item_icon)

        //--- Инициализировать ViewModel
//        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        // цвета для TextView и ImageView
        colorNew = ContextCompat.getColor(this, R.color.icon_menu_tap)
        colorOld = ContextCompat.getColor(this, R.color.icon_menu_default)

        //=== инициализация ViewPager2 и адаптера
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val adapterPager2 = ViewPagerAdapter(this)
        viewPager.adapter = adapterPager2

        //=== индикатор фрагмента
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        // Инициализируем TabLayoutMediator и сохраняем его в переменную
        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "----------------" // "Page - ${position + 1}" // Установите текст вкладки или иконку
        }
        tabLayoutMediator.attach()
        // Задаем цвет текста в вкладках
        val normalColor = ContextCompat.getColor(this, R.color.edit_title) // цвет текста неактивной вкладки
        val selectedColor = ContextCompat.getColor(this, R.color.icon_menu_default) // цвет текста активной вкладки

        tabLayout.setTabTextColors(normalColor, selectedColor)
        // Изменение текста на вкладках по индексу
        fun updateTabText(index: Int, newText: String) {
            val tab = tabLayout.getTabAt(index)
            tab?.text = newText
        }
        // Функция для программного выбора вкладки по индексу
        fun selectTab(index: Int) {
            // Проверяем, что индекс находится в пределах допустимого диапазона
            if (index in 0 until tabLayout.tabCount) {
                val tab = tabLayout.getTabAt(index)
                tab?.select() // Выбираем вкладку
            }
        }
        //=== код вынесен из тела кликов по выбору фрагмента по вкладкам в активити =========================================
        fun clickFragmentCase() {
            adapterPager2.updateData(boardList) // список портфелей
            selectTab(0) // Переключение на первую вкладку (индекс 0)
            // Изменение текста (board) на вкладках по индексу листа
            for (index in boardList.indices)
                updateTabText(index, "${index + 1} ' ${boardList[index]} ' ")
            updateColors("FragmentCase") // меняем цвет титла и иконки
            resetBoards() // сбрасываем боард
            trueBoard("-------") // устанавливаем новый боард
            // открываем выбранный фрагмент
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FragmentCase())
                .addToBackStack(null)
                .commit()
        }
        //=== код вынесен из тела кликов по выбору фрагмента по вкладкам в активити =========================================
        fun click(
            ticker: String = boardList[0],
            fragment: String = nameFragment,
            titleFragment: String = tv_title,
            flag: Boolean = false
        ) {
            if (!flag) {
                adapterPager2.updateData(boardList) // список боардов
                selectTab(0) // Переключение на первую вкладку (индекс 0)
                // Изменение текста (board) на вкладках по индексу листа
                for (index in boardList.indices)
                    updateTabText(index, "${index + 1} ' ${boardList[index]} ' ")
                nameFragment = fragment // фрагмент по сектору (акции, облигации)
                tv_title = titleFragment // имя эмитента фрагмента
                updateColors(nameFragment) // меняем цвет титла и иконки
            }
            resetBoards() // сбрасываем боард
            trueBoard(ticker) // устанавливаем новый боард

            if (!flag) {
//                replaceFragment(FragmentTicker()) // очередность транзакций
            } else { // вызываем фрагмент с новыми данными// Обновляем список в нашей программе
                if (isSecurities || isMarketData) {
                    isSecurities = false // отменяем установленный флаг для показа итемов Securities
                    isMarketData = false // отменяем установленный флаг для показа итемов MarketData
                }

                // открываем выбранный фрагмент
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FragmentTicker())
                    .addToBackStack(null)
                    .commit()
            }
        }
        // Слушатель для выбора вкладок
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val regex = "'\\s*(.*?)\\s*'".toRegex() // патерн
                val text = tab.text.toString()
                val result = regex.find(text)?.groupValues?.get(1) ?: ""

                // Действие при выборе вкладки
                click(ticker = result, flag = true) // вызвать функцию дополнительного кода
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Действие при отмене выбора вкладки
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                // Действие при повторном выборе той же вкладки
            }
        })

        // Если нужно настроить поведение свайпа, например, отключить предзагрузку фрагментов:
        viewPager.offscreenPageLimit = 1

        // меняем цвет титла и иконки
        updateColors("ActivityHome")

        // Создание папки 'InvestmentMonitor' в "Загрузках"
        createDownloadsFolder(this)

        // База данных
//        dbHelper = DatabaseHelper(this)

        homeItem.setOnClickListener {
            // Переход к новой активности
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            nameFragment = "ActivityHome"
            
            // меняем цвет титла и иконки
            updateColors(nameFragment)
        }
        caseItem.setOnClickListener {
            // вызов фрагмента с заголовком и итемами
            boardList = mutableListOf("Портфель") // количество страниц в viewPager2
            clickFragmentCase() // вызвать функцию дополнительного кода
        }
        stockItem.setOnClickListener {
            boardList = mutableListOf("TQBR", "SMAL", "SPEQ") // количество страниц в viewPager2
            click("TQBR", "FragmentStocks", "Акции") // вызвать функцию дополнительного кода            FragmentTicker() // вызываем фрагмент с новыми данными// Обновляем список в нашей программе
        }
        fundItem.setOnClickListener {
            boardList = mutableListOf("TQTF", "TQPI", "TQIF") // количество страниц в viewPager2
            click("TQTF", "FragmentFunds", "Фонды") // вызвать функцию дополнительного кода
        }
        bondItem.setOnClickListener {
            boardList = mutableListOf("TQOB", "TQCB", "PACT", "SPOB", "TQIR", "TQIY", "TQOD", "TQOE",
                "TQOY", "TQRD", "TQUD") // количество страниц в viewPager2
            click("TQOB", "FragmentBonds", "Облигации") // вызвать функцию дополнительного кода
        }
        indexItem.setOnClickListener {
            boardList = mutableListOf("SNDX", "RTSI", "INAV", "INPF") // количество страниц в viewPager2
            click("SNDX", "FragmentIndexes", "Индексы") // вызвать функцию дополнительного кода
        }
        futuresItem.setOnClickListener {
            boardList = mutableListOf("RFUD") // количество страниц в viewPager2
            click("RFUD", "FragmentFutures", "Фьючерсы") // вызвать функцию дополнительного кода
        }
    }

    // транзакции фрагментов
//    private fun replaceFragment(fragment: Fragment) {
//        // Выполняем транзакцию замены фрагмента
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.fragment_container, fragment)
//        fragmentTransaction.addToBackStack(null)  // Добавляем в стек, чтобы можно было вернуться назад
//        fragmentTransaction.commit()
//
//        // открываем выбранный фрагмент
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, fragment)
//            .commit()
//    }

    // Переопределяем кнопку "Назад"
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.fragments.size > 0) {
            val transaction = fragmentManager.beginTransaction()

            if (!isSecurities && !isMarketData) {
                // Удаление всех активных фрагментов
                for (fragment in fragmentManager.fragments)
                    if (fragment != null)
                        transaction.remove(fragment)
                transaction.commit()
                // Очистка back stack
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                updateColors("ActivityHome")
            } else {
                isSecurities = false // отменяем установленный флаг для показа итемов Securities
                isMarketData = false // отменяем установленный флаг для показа итемов MarketData
                // Возвращаемся к предыдущему фрагменту
                fragmentManager.popBackStack()
            }
        } else {
            // Если в стеке нет фрагментов, выполняем стандартное действие
            super.onBackPressed()
        }
    }

    // Функция для изменения цвета иконок и текста меню
    fun updateColors(fragmentType: String) {
        // текст по умолчанию
        menuHomeText.setTextColor(colorOld)
        menuCaseText.setTextColor(colorOld)
        menuIndexText.setTextColor(colorOld)
        menuStockText.setTextColor(colorOld)
        menuFundText.setTextColor(colorOld)
        menuBondText.setTextColor(colorOld)
        menuFuturesText.setTextColor(colorOld)
        // иконки
        menuHomeIcon.setColorFilter(colorOld)
        menuCaseIcon.setColorFilter(colorOld)
        menuIndexIcon.setColorFilter(colorOld)
        menuStockIcon.setColorFilter(colorOld)
        menuFundIcon.setColorFilter(colorOld)
        menuBondIcon.setColorFilter(colorOld)
        menuFuturesIcon.setColorFilter(colorOld)

        // Останавливаем корутину, когда активность не активена
        job?.cancel()

        menuStockIcon.setImageResource(R.drawable.chart0)
        menuFundIcon.setImageResource(R.drawable.chart0)
        menuBondIcon.setImageResource(R.drawable.chart0)
        menuIndexIcon.setImageResource(R.drawable.chart0)
        menuFuturesIcon.setImageResource(R.drawable.chart0)

        // анимация иконки
        fun animeChart(imageView: ImageView) {
            // Останавливаем корутину предыдущего вызова
            job?.cancel()
            if (imageView == menuHomeIcon || imageView == menuCaseIcon) {
                return
            }
            var count = 0
            // Запускаем корутину для повторного вызова fetchTickersStocks()
            job = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    when (count) {
                        0 -> imageView.setImageResource(R.drawable.chart1)
                        1 -> imageView.setImageResource(R.drawable.chart0)
                        2 -> imageView.setImageResource(R.drawable.chart1)
                        3 -> imageView.setImageResource(R.drawable.chart2)
                        4 -> imageView.setImageResource(R.drawable.chart1)
                        5 -> imageView.setImageResource(R.drawable.chart2)
                        6 -> imageView.setImageResource(R.drawable.chart3)
                        7 -> imageView.setImageResource(R.drawable.chart2)
                        8 -> imageView.setImageResource(R.drawable.chart3)
                        9 -> imageView.setImageResource(R.drawable.chart4)
                        10 -> imageView.setImageResource(R.drawable.chart3)
                        11 -> imageView.setImageResource(R.drawable.chart4)
                        12 -> imageView.setImageResource(R.drawable.chart3)
                        13 -> imageView.setImageResource(R.drawable.chart4)
                        14 -> imageView.setImageResource(R.drawable.chart3)
                        15 -> imageView.setImageResource(R.drawable.chart2)
                        16 -> imageView.setImageResource(R.drawable.chart3)
                        17 -> imageView.setImageResource(R.drawable.chart2)
                        18 -> imageView.setImageResource(R.drawable.chart3)
                        19 -> imageView.setImageResource(R.drawable.chart2)
                        20 -> imageView.setImageResource(R.drawable.chart1)
                        21 -> imageView.setImageResource(R.drawable.chart2)
                        22 -> imageView.setImageResource(R.drawable.chart1)
                        23 -> imageView.setImageResource(R.drawable.chart2)
                        24 -> imageView.setImageResource(R.drawable.chart1)
                        25 -> imageView.setImageResource(R.drawable.chart0)
                    }
                    if (count >= 25) count = 0
                    else count += 1

                    // Задержка на 1 минуту (60000 мс)
                    delay(600)
                }
            }
        }

        // изменить цвет текста и иконок в меню
        when (fragmentType) {
            "ActivityHome" -> {
                menuHomeText.setTextColor(colorNew)
                menuHomeIcon.setColorFilter(colorNew)
            }
            "FragmentCase" -> {
                menuCaseText.setTextColor(colorNew)
                menuCaseIcon.setColorFilter(colorNew)
            }
            "FragmentStocks" -> {
                menuStockText.setTextColor(colorNew)
                menuStockIcon.setColorFilter(colorNew)
                animeChart(menuStockIcon)
            }
            "FragmentFunds" -> {
                menuFundText.setTextColor(colorNew)
                menuFundIcon.setColorFilter(colorNew)
                animeChart(menuFundIcon)
            }
            "FragmentBonds" -> {
                menuBondText.setTextColor(colorNew)
                menuBondIcon.setColorFilter(colorNew)
                animeChart(menuBondIcon)
            }
            "FragmentIndexes" -> {
                menuIndexText.setTextColor(colorNew)
                menuIndexIcon.setColorFilter(colorNew)
                animeChart(menuIndexIcon)
            }
            "FragmentFutures" -> {
                menuFuturesText.setTextColor(colorNew)
                menuFuturesIcon.setColorFilter(colorNew)
                animeChart(menuFuturesIcon)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // Останавливаем корутину, когда фрагмент не активен
        job?.cancel()
    }

}
