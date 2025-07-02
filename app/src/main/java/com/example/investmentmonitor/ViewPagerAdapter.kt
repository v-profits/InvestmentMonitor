package com.example.investmentmonitor

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.investmentmonitor.MOEX.FragmentTicker

//import com.example.investmentmonitor.bonds.FragmentBond
//import com.example.investmentmonitor.case.FragmentCase
//import com.example.investmentmonitor.funds.FragmentFund
//import com.example.investmentmonitor.futures.FragmentFutures
//import com.example.investmentmonitor.stocks.FragmentStock

//=== Создайте класс PagerAdapter, который будет управлять фрагментами в ViewPager2
class ViewPagerAdapter(val activity: MainActivity) : FragmentStateAdapter(activity) {
    private var dataList = mutableListOf<String>() // Ваши данные

    override fun getItemCount(): Int {
        // Количество фрагментов, которые вы хотите отобразить
        return dataList.size
    }

    override fun createFragment(position: Int): Fragment {
        // Создание фрагмента с использованием данных из dataList
        return FragmentTicker.newInstance(dataList[position])
    }

    // Метод для обновления данных
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<String>) {
        dataList.clear()
        dataList.addAll(newData)
        notifyDataSetChanged() // Уведомляем адаптер о смене данных
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

////Если вы используете FragmentStateAdapter для управления фрагментами, , чтобы получить текущий активный фрагмент:
//val currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
//
//// в каждом фрагменте
//override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//    super.setUserVisibleHint(isVisibleToUser)
//    if (isVisibleToUser) {
//        // Фрагмент стал видимым
//    }
//}
//
////ViewPager2 предоставляет возможность отслеживать изменения страницы
//viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//    override fun onPageSelected(position: Int) {
//        super.onPageSelected(position)
//        // position — индекс текущего фрагмента
//        Log.d("ViewPager", "Current fragment position: $position")
//        // Можете использовать position для получения фрагмента
//        val currentFragment = fragmentList[position]
//    }
//})
//
//// в каждом фрагменте
//if (fragment.isVisible) {
//    // Фрагмент виден пользователю
//}
//
//// в каждом фрагменте
//override fun onResume() {
//    super.onResume()
//    // Этот фрагмент сейчас активен
//}
//
//
////ViewPager2 предоставляет возможность отслеживать изменения страницы
//// В этом случае отслеживается не сам фрагмент, а его позиция.
//viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//    override fun onPageSelected(position: Int) {
//        super.onPageSelected(position)
//        if (position == fragmentPosition) {
//            // Фрагмент активен
//        }
//    }
//})
