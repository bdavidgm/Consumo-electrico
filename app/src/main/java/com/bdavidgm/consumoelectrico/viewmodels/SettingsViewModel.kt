package com.bdavidgm.consumoelectrico.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdavidgm.consumoelectrico.datastore.SettingsRepository
import com.bdavidgm.consumoelectrico.datastore.SettingsUiState
import com.bdavidgm.consumoelectrico.datastore.ViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class para las configuraciones

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository): ViewModel()
{

    // Estado principal de la UI
    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Estados para inputs temporales
    private val _newEmailInput = MutableStateFlow("")
    val newEmailInput: StateFlow<String> = _newEmailInput.asStateFlow()

    private val _senderEmailInput = MutableStateFlow("")
    val senderEmailInput: StateFlow<String> = _senderEmailInput.asStateFlow()

    private val _senderPasswordInput = MutableStateFlow("")
    val senderPasswordInput: StateFlow<String> = _senderPasswordInput.asStateFlow()

    // Eventos para UI (Snackbars, etc.)
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadSettings()
    }

    // Observar cambios en los settings del repository
    private fun loadSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.settingsFlow
                .collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        reportEmails = settings.reportEmails,
                        senderEmail = settings.senderEmail,
                        senderPassword = "", // Por seguridad no mostramos la contraseña
                        viewMode = settings.viewMode,
                        isLoading = false
                    )
                    // Sincronizar inputs con los valores guardados
                    _senderEmailInput.value = settings.senderEmail
                }
        }
    }

    // Actions desde la UI
    fun onNewEmailInputChanged(email: String) {
        _newEmailInput.value = email
    }

    fun onSenderEmailInputChanged(email: String) {
        _senderEmailInput.value = email
    }

    fun onSenderPasswordInputChanged(password: String) {
        _senderPasswordInput.value = password
    }

    fun addReportEmail() {
        val email = _newEmailInput.value.trim()
        if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            viewModelScope.launch {
                try {
                    repository.addReportEmail(email)
                    _newEmailInput.value = ""
                    showSnackbar("Email añadido: $email")
                } catch (e: Exception) {
                    showSnackbar("Error al añadir email: ${e.message}")
                }
            }
        } else {
            showSnackbar("Email inválido")
        }
    }

    fun removeReportEmail(email: String) {
        viewModelScope.launch {
            try {
                repository.removeReportEmail(email)
                showSnackbar("Email eliminado: $email")
            } catch (e: Exception) {
                showSnackbar("Error al eliminar email: ${e.message}")
            }
        }
    }

    fun saveSenderCredentials() {
        val email = _senderEmailInput.value.trim()
        val password = _senderPasswordInput.value

        if (email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.isNotEmpty()) {
                viewModelScope.launch {
                    try {
                        repository.saveSenderCredentials(email, password)
                        _senderPasswordInput.value = "" // Limpiar campo después de guardar
                        showSnackbar("Credenciales guardadas y encriptadas")
                    } catch (e: Exception) {
                        showSnackbar("Error al guardar credenciales: ${e.message}")
                    }
                }
            } else {
                showSnackbar("La contraseña no puede estar vacía")
            }
        } else {
            showSnackbar("Email del remitente inválido")
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.setViewMode(viewMode)
            } catch (e: Exception) {
                showSnackbar("Error al cambiar modo de vista: ${e.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }
}

