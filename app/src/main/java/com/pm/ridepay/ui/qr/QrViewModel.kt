package com.pm.ridepay.ui.qr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pm.ridepay.data.local.ImageSaver
import com.pm.ridepay.data.repository.DriverRepository
import com.pm.ridepay.util.toUserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class QrUiState(
    val isLoading: Boolean=true,
    val qrBitmap: Bitmap?=null,
    val errorMessage: String?=null,
    val isSaving: Boolean=false,
    val saveMessage: String?=null
)





class QrViewModel(
    private val driverRepository: DriverRepository,
    private val imageSaver: ImageSaver,
    private val qrImageUrl: String?
): ViewModel() {
    private val _uiState= MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    private var imageBytes: ByteArray? =null

    init {
        load()
    }

    fun load(){
        val url=qrImageUrl
        if(url.isNullOrBlank()){
            _uiState.value=QrUiState(isLoading = false, errorMessage = "Your QR is not ready yet.")
            return
        }

        _uiState.value= QrUiState(isLoading = true)

        viewModelScope.launch {
            val result=driverRepository.loadQrImage(url)
            val bytes=result.getOrNull()
            if(bytes==null){
                val message=result.exceptionOrNull()?.toUserMessage()
                    ?: "Could not load the QR."
                _uiState.value= QrUiState(isLoading = false, errorMessage = message)
                return@launch
            }

            val bitmap= withContext(Dispatchers.Default){
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            if(bitmap==null){
                _uiState.value= QrUiState(isLoading = false, errorMessage = "Could not read the QR image.")
            }
            else{
                imageBytes=bytes
                _uiState.value= QrUiState(isLoading = false, qrBitmap = bitmap)
            }
        }
    }


    fun download(){
        val bytes=imageBytes?: return
        if(_uiState.value.isSaving) return
        _uiState.update {
            it.copy(isSaving = true, saveMessage = null)
        }

        viewModelScope.launch {
            val result=imageSaver.savePng(bytes, "ridepay-qr")
            _uiState.update {
                it.copy(isSaving = false,
                    saveMessage = if(result.isSuccess){
                        "Saved to Pictures/RidePay"
                    }
                    else{
                        "Could not save the image"
                    }
                )
            }
        }
    }
}


class QrViewModelFactory(
    private val driverRepository: DriverRepository,
    private val imageSaver: ImageSaver,
    private val qrImageUrl: String?
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        QrViewModel(driverRepository, imageSaver, qrImageUrl) as T
}