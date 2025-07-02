package com.example.investmentmonitor.MOEX

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.investmentmonitor.R
import com.example.investmentmonitor.value_moex.getValuesAndColumnNames
import com.example.investmentmonitor.value_moex.FragmentData

class FragmentMenu : Fragment() {

    private lateinit var viewModel: ViewModel

    private lateinit var newHeader: FrameLayout
    private lateinit var skipUp: FrameLayout
    private lateinit var skipDown: FrameLayout
    private lateinit var securities: FrameLayout
    private lateinit var marketdata: FrameLayout
    private lateinit var ticker: TextView
    // цвета флажков
    private lateinit var colorImage1: FrameLayout
    private lateinit var colorImage2: FrameLayout
    private lateinit var colorImage3: FrameLayout
    private lateinit var colorImage4: FrameLayout
    private lateinit var colorImage5: FrameLayout
    private lateinit var colorImage6: FrameLayout

    private lateinit var title: FrameLayout
    private lateinit var editText: EditText
    private lateinit var panelColor: LinearLayout
    private lateinit var delete: FrameLayout

    companion object {
        private const val ARG_POSITION = "position"

        // так
//        fun newInstance(ticker: StockTickerResponse, position: Int): FragmentMenuStock {
//            val fragment = FragmentMenuStock()
//            val args = Bundle()
//            args.putInt(ARG_POSITION, position)
//            fragment.arguments = args
//            return fragment
//        }
        // или так
        fun newInstance(ticker: TickerResponse, position: Int): FragmentMenu {
            return FragmentMenu().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val position = arguments?.getInt(ARG_POSITION) ?: 0

        // инициализация виджетов
        newHeader = view.findViewById(R.id.new_header)
        skipUp = view.findViewById(R.id.skip_up)
        skipDown = view.findViewById(R.id.skip_down)
        securities = view.findViewById(R.id.securities)
        marketdata = view.findViewById(R.id.marketdata)
        ticker = view.findViewById(R.id.ticker_menu)
        // цвета флажков
        colorImage1 = view.findViewById(R.id.color_image1)
        colorImage2 = view.findViewById(R.id.color_image2)
        colorImage3 = view.findViewById(R.id.color_image3)
        colorImage4 = view.findViewById(R.id.color_image4)
        colorImage5 = view.findViewById(R.id.color_image5)
        colorImage6 = view.findViewById(R.id.color_image6)

        title = view.findViewById(R.id.title)
        panelColor = view.findViewById(R.id.panel_color)
        delete = view.findViewById(R.id.delete)
        editText = view.findViewById(R.id.edit_title)

        fun updateView() {
            if (currentPosition != null) {

                if (backupTicker[currentPosition!!].visibilityTitle) {

                    title.visibility = View.GONE
                    panelColor.visibility = View.GONE
                    newHeader.visibility = View.VISIBLE
//                    skipUp.visibility = View.VISIBLE
//                    skipDown.visibility = View.VISIBLE
                    securities.visibility = View.GONE
                    marketdata.visibility = View.GONE
                    delete.visibility = View.VISIBLE
                    ticker.visibility = View.GONE

                } else {

                    title.visibility = View.VISIBLE
                    panelColor.visibility = View.VISIBLE
                    newHeader.visibility = View.GONE
//                    skipUp.visibility = View.VISIBLE
//                    skipDown.visibility = View.VISIBLE
                    securities.visibility = View.VISIBLE
                    marketdata.visibility = View.VISIBLE
                    delete.visibility = View.GONE
                    ticker.visibility = View.VISIBLE
                }
            }
        }

        // скрываем виджеты
        updateView()

        // Запрашивает фокус для EditText (мигающий курсор)
        editText.requestFocus()
        // Устанавливает текст в EditText
        if (currentPosition != null) {
            if (backupTicker[currentPosition!!].titleItem != "Заголовок") {
                editText.setText(backupTicker[currentPosition!!].titleItem)  // Устанавливает текст в EditText
            }
        }

        // Удаление текущего фрагмента
        fun removeFragment() {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }

        // Устанавливаем слушатель на клавишу "Ввод"
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                // Закрытие фрагмента
//                requireActivity().supportFragmentManager.popBackStack()
                removeFragment() // Удаление текущего фрагмента
                true
            } else {
                false
            }
        }

        // Используйте индекс для отображения данных
        ticker.text = "Тикер:   ${backupTicker[position].ticker}"

        //--- отслеживаем нажатие на значек цвета в диалоговом окне и передаем цвет в фрагмент
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)

        // клики
        // Установка слушателей кликов

        // Обработка клика по элементу "Добавить раздел выше"
        newHeader.setOnClickListener {
            removeFragment() // Удаление текущего фрагмента
        }

        // Обработка клика по элементу "Добавить раздел выше"
        title.setOnClickListener {
            // Создаем новый заголовок
            val newHeader = TickerResponse(
                board = "",
                ticker = "Title",
                name = "",
                decimals = 0,
                currentPrice = "",
                lastPrice = "",
                simbolBanca = "",
                flagColor = null, // Поле для цвета
                archiveColor = null, // Поле для хранения цвета
                visibilityTitle = false,
                titleItem = "Заголовок"
            )

            // Добавляем заголовок в список backupTickerStocks на текущую позицию
            backupTicker.add(currentPosition ?: 0, newHeader)

            if (currentPosition != null)
                backupTicker[currentPosition!!].visibilityTitle = true

            // скрываем виджеты
            updateView()
//            removeFragment() // Удаление текущего фрагмента
        }

        // Обработка клика по элементу "Удалить заголовок"
        delete.setOnClickListener {
            // Удаляем заголовок
            if (currentPosition != null) {
                backupTicker.removeAt(currentPosition!!)
                if (backupTicker.size == currentPosition && currentPosition != 0) {
                    currentPosition = currentPosition!! - 1
                }
            }
            removeFragment() // Удаление текущего фрагмента
        }
        // Обработка клика по элементу "Переместить вверх"
        skipUp.setOnClickListener {
            // Меняем элемент на позицию 0
            isSave = false
            for (i in position downTo 1) {
                if (i == 1) isSave = true
                viewModel.swapItems(i, i-1)
            }
            removeFragment() // Удаление текущего фрагмента
        }
        // Обработка клика по элементу "Переместить вниз"
        skipDown.setOnClickListener {
            // Меняем элемент на позицию size
            isSave = false
            for (i in position until  backupTicker.size-1) {
                if (i == backupTicker.size-2) isSave = true
                viewModel.swapItems(i, i+1)
            }
            removeFragment() // Удаление текущего фрагмента
        }
        // передаем цвет флажка для изменения в итеме фрагмента
        colorImage1.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag1))
            removeFragment() // Удаление текущего фрагмента
        }
        colorImage2.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag2))
            removeFragment() // Удаление текущего фрагмента
        }
        colorImage3.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag3))
            removeFragment() // Удаление текущего фрагмента
        }
        colorImage4.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag4))
            removeFragment() // Удаление текущего фрагмента
        }
        colorImage5.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag5))
            removeFragment() // Удаление текущего фрагмента
        }
        colorImage6.setOnClickListener {
            viewModel.setColor(ContextCompat.getColor(requireContext(), R.color.flag6))
            removeFragment() // Удаление текущего фрагмента
        }
        // Обработка клика по элементу Securities
        securities.setOnClickListener {
            isSecurities = true // устанавливаем флаг для показа итемов Securities
            // Создаем новый экземпляр фрагмента FragmentData
            val fragmentData = FragmentData()
            // Инициализируем Bundle для передачи данных
            val bundle = Bundle()
            // Вызываем функцию для получения данных
            getValuesAndColumnNames(backupTicker[position].board, backupTicker[position].ticker) { result ->
                // Преобразуем двумерный список в Serializable (или Parcelable)
                bundle.putSerializable("ticker_data", ArrayList(result))
                // Передаем данные в FragmentData
                fragmentData.arguments = bundle
                // создаем фрагмент с данными тикера
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragmentData)
                    .addToBackStack(null)
                    .commit()
            }
            removeFragment() // Удаление текущего фрагмента
        }
        // Обработка клика по элементу MarketData
        marketdata.setOnClickListener {
            isMarketData = true // устанавливаем флаг для показа итемов MarketData
            val fragmentData = FragmentData()
            val bundle = Bundle()
            getValuesAndColumnNames(backupTicker[position].board, backupTicker[position].ticker) { result ->
                bundle.putSerializable("ticker_data", ArrayList(result))
                fragmentData.arguments = bundle
                // создаем фрагмент с данными тикера
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragmentData)
                    .addToBackStack(null)
                    .commit()
            }
            removeFragment() // Удаление текущего фрагмента
        }

        // Убедитесь, что фрагмент перехватывает нажатия
        view?.setOnClickListener {
            // Ничего не делаем, чтобы перехватить нажатия
            removeFragment() // Удаление текущего фрагмента
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        // отслеживаем инициализацию переменных введенного титла и чека окна ввода, и передача значений
        if (editText.text.toString() == "") {
            viewModel.setTitle(titleItem) // текст по умолчанию
        } else {
            viewModel.setTitle(editText.text.toString()) // введеный текст
        }
    }
}
