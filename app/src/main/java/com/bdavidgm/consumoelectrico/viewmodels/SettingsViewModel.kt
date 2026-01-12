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

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {

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

    // Estado para controlar visibilidad de contraseña guardada
    private val _showSavedPassword = MutableStateFlow(false)
    val showSavedPassword: StateFlow<Boolean> = _showSavedPassword.asStateFlow()

    // Estado para controlar visibilidad de contraseña en campo de entrada
    private val _showInputPassword = MutableStateFlow(false)
    val showInputPassword: StateFlow<Boolean> = _showInputPassword.asStateFlow()

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
                        // Mostrar asteriscos si hay contraseña guardada
                        senderPassword = if (settings.senderPassword.isNotEmpty()) {
                            "••••••••"
                        } else {
                            ""
                        },
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

    // Alternar visibilidad de contraseña guardada
    fun toggleSavedPasswordVisibility() {
        _showSavedPassword.value = !_showSavedPassword.value
        // Opcional: Ocultar automáticamente después de 10 segundos
        if (_showSavedPassword.value) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(10000)
                _showSavedPassword.value = false
            }
        }
    }

    // Alternar visibilidad de contraseña en campo de entrada
    fun toggleInputPasswordVisibility() {
        _showInputPassword.value = !_showInputPassword.value
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
                        _showInputPassword.value = false // Ocultar contraseña
                        showSnackbar("Credenciales guardadas de forma segura")
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

    // Nuevo método para obtener la contraseña actual (para uso interno)
    suspend fun getCurrentPassword(): String {
        return try {
            repository.getCurrentSettings().senderPassword
        } catch (e: Exception) {
            ""
        }
    }

    // Método para verificar si hay contraseña guardada
    fun hasPassword(): Boolean {
        return repository.hasPassword()
    }

    // Método para eliminar la contraseña guardada
    fun clearPassword() {
        viewModelScope.launch {
            try {
                repository.clearPassword()
                showSnackbar("Contraseña eliminada de forma segura")
                // Actualizar UI State para reflejar el cambio
                _uiState.value = _uiState.value.copy(
                    senderPassword = ""
                )
            } catch (e: Exception) {
                showSnackbar("Error al eliminar contraseña: ${e.message}")
            }
        }
    }

    // Método para guardar todos los settings
    suspend fun saveAllSettings(settings: com.bdavidgm.consumoelectrico.datastore.AppSettings) {
        try {
            repository.saveSettings(settings)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    // Propiedad computada para obtener la contraseña a mostrar
    val displayedPassword: StateFlow<String> = combine(
        _uiState,
        _showSavedPassword
    ) { state, show ->
        if (state.senderPassword.isNotEmpty() && show) {
            // Si hay contraseña y se debe mostrar, obtenerla del repositorio
            // Esto es una simplificación - en la práctica, necesitarías obtenerla de manera asíncrona
            // O mantener una copia en el ViewModel
            "••••••••" // Placeholder - implementar lógica real según necesidades
        } else {
            state.senderPassword
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )
}