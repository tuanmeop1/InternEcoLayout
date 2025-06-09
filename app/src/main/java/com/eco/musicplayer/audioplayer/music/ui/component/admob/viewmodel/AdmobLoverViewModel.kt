package com.eco.musicplayer.audioplayer.music.ui.component.admob.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eco.musicplayer.audioplayer.music.utils.CoinStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdmobLoverViewModel(private val coinStorage: CoinStorage): ViewModel() {

    private val _coins = MutableLiveData<Int>()
    val coins: LiveData<Int> get() = _coins

    init {
        viewModelScope.launch {
            val current = withContext(Dispatchers.IO) {
                coinStorage.getCoin()
            }
            _coins.value = current
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            val newAmount = withContext(Dispatchers.IO) {
                coinStorage.addCoin(amount)
                coinStorage.getCoin()
            }
            _coins.postValue(newAmount)
        }
    }
}