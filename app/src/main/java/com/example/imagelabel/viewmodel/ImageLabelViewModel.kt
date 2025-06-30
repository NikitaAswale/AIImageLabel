package com.example.imagelabel.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagelabel.ml.ImageLabelingService
import com.example.imagelabel.ml.LabelResult
import com.example.imagelabel.ml.ObjectResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(
    val selectedImage: Bitmap? = null,
    val labels: List<LabelResult> = emptyList(),
    val objects: List<ObjectResult> = emptyList(),
    val isProcessing: Boolean = false,
    val processingType: ProcessingType = ProcessingType.NONE,
    val error: String? = null,
    val showImageSourceDialog: Boolean = false
)

enum class ProcessingType {
    NONE, LABELING, OBJECT_DETECTION
}

class ImageLabelViewModel : ViewModel() {
    
    private val imageLabelingService = ImageLabelingService()
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun selectImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            selectedImage = bitmap,
            labels = emptyList(),
            objects = emptyList(),
            error = null
        )
    }
    
    fun labelImage() {
        val bitmap = _uiState.value.selectedImage ?: return
        
        _uiState.value = _uiState.value.copy(
            isProcessing = true,
            processingType = ProcessingType.LABELING,
            error = null
        )
        
        viewModelScope.launch {
            imageLabelingService.labelImage(bitmap)
                .onSuccess { labels ->
                    _uiState.value = _uiState.value.copy(
                        labels = labels,
                        isProcessing = false,
                        processingType = ProcessingType.NONE
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to label image: ${exception.message}",
                        isProcessing = false,
                        processingType = ProcessingType.NONE
                    )
                }
        }
    }
    
    fun detectObjects() {
        val bitmap = _uiState.value.selectedImage ?: return
        
        _uiState.value = _uiState.value.copy(
            isProcessing = true,
            processingType = ProcessingType.OBJECT_DETECTION,
            error = null
        )
        
        viewModelScope.launch {
            imageLabelingService.detectObjects(bitmap)
                .onSuccess { objects ->
                    _uiState.value = _uiState.value.copy(
                        objects = objects,
                        isProcessing = false,
                        processingType = ProcessingType.NONE
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to detect objects: ${exception.message}",
                        isProcessing = false,
                        processingType = ProcessingType.NONE
                    )
                }
        }
    }
    
    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            labels = emptyList(),
            objects = emptyList(),
            error = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun showImageSourceDialog() {
        _uiState.value = _uiState.value.copy(showImageSourceDialog = true)
    }
    
    fun hideImageSourceDialog() {
        _uiState.value = _uiState.value.copy(showImageSourceDialog = false)
    }
    
    override fun onCleared() {
        super.onCleared()
        imageLabelingService.close()
    }
} 