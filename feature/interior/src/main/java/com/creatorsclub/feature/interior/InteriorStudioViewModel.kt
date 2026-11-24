package com.creatorsclub.feature.interior

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class InteriorStudioUiState(
    val roomWidthMeters: Float = 5.0f,
    val roomHeightMeters: Float = 4.0f,
    val wallColor: Color = Color(0xFFF7F8FC),
    val floorColor: Color = Color(0xFFEEF1F7),
    val placedFurnitureList: List<PlacedFurniture> = emptyList(),
    val selectedFurnitureId: String? = null
)

@HiltViewModel
class InteriorStudioViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(InteriorStudioUiState())
    val uiState: StateFlow<InteriorStudioUiState> = _uiState.asStateFlow()

    fun addFurniture(type: FurnitureType) {
        val newFurniture = PlacedFurniture(
            id = "furn_${UUID.randomUUID().toString().take(6)}",
            type = type,
            x = 160f,
            y = 160f
        )
        _uiState.update {
            it.copy(
                placedFurnitureList = it.placedFurnitureList + newFurniture,
                selectedFurnitureId = newFurniture.id
            )
        }
    }

    fun selectFurniture(id: String?) {
        _uiState.update { it.copy(selectedFurnitureId = id) }
    }

    fun updatePosition(id: String, x: Float, y: Float) {
        _uiState.update { state ->
            val updated = state.placedFurnitureList.map { f ->
                if (f.id == id) f.copy(x = x, y = y) else f
            }
            state.copy(placedFurnitureList = updated)
        }
    }

    fun rotateSelected() {
        val id = _uiState.value.selectedFurnitureId ?: return
        _uiState.update { state ->
            val updated = state.placedFurnitureList.map { f ->
                if (f.id == id) f.copy(rotation = (f.rotation + 45f) % 360f) else f
            }
            state.copy(placedFurnitureList = updated)
        }
    }

    fun deleteSelected() {
        val id = _uiState.value.selectedFurnitureId ?: return
        _uiState.update { state ->
            state.copy(
                placedFurnitureList = state.placedFurnitureList.filterNot { it.id == id },
                selectedFurnitureId = null
            )
        }
    }

    fun setWallColor(color: Color) {
        _uiState.update { it.copy(wallColor = color) }
    }

    fun setFloorColor(color: Color) {
        _uiState.update { it.copy(floorColor = color) }
    }
}
