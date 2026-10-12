package com.example.kittgrowcontrol.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kittgrowcontrol.data.Crop
import com.example.kittgrowcontrol.data.DataRepository
import com.example.kittgrowcontrol.data.WateringLog
import com.example.kittgrowcontrol.ui.main.MainScreenUiState.Success
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainScreenViewModel(private val dataRepository: DataRepository) : ViewModel() {
  
  val uiState: StateFlow<MainScreenUiState> =
    dataRepository.data
      .map<List<Crop>, MainScreenUiState> { Success(it) }
      .catch { emit(MainScreenUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainScreenUiState.Loading)

  fun addCrop(crop: Crop) {
    dataRepository.addCrop(crop)
  }

  fun updateCrop(crop: Crop) {
    dataRepository.updateCrop(crop)
  }

  fun deleteCrop(cropId: Long) {
    dataRepository.deleteCrop(cropId)
  }

  fun logWatering(cropId: Long, log: WateringLog) {
    dataRepository.logWatering(cropId, log)
  }

  fun logEnvironment(cropId: Long, temp: Double, humidity: Double) {
    dataRepository.logEnvironment(cropId, temp, humidity)
  }

  fun changeState(cropId: Long, newState: String, date: String, quantity: Int) {
    dataRepository.changeState(cropId, newState, date, quantity)
  }

  fun harvestCrop(cropId: Long, date: String, quantity: Int, pesoFresco: Double) {
    dataRepository.harvestCrop(cropId, date, quantity, pesoFresco)
  }

  fun updateHarvestState(cropId: Long, subEstado: String, yieldGrams: Double, ratingStars: Int, notes: String, date: String) {
    dataRepository.updateHarvestState(cropId, subEstado, yieldGrams, ratingStars, notes, date)
  }

  fun exportJson(): String {
    return dataRepository.exportJson()
  }

  fun importJson(jsonString: String): Boolean {
    return dataRepository.importJson(jsonString)
  }
}

sealed interface MainScreenUiState {
  object Loading : MainScreenUiState

  data class Error(val throwable: Throwable) : MainScreenUiState

  data class Success(val data: List<Crop>) : MainScreenUiState
}
