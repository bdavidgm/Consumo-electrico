package com.bdavidgm.consumoelectrico.datastore

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.minus
import kotlin.collections.plus
import javax.inject.Inject

data class AppSettings(
    val reportEmails: Set<String> = emptySet(),
    val senderEmail: String = "",
    val senderPassword: String = "", // Contraseña encriptada
    val viewMode: ViewMode = ViewMode.MONTHLY
)

// Enumerado para los modos de vista
enum class ViewMode {
    MONTHLY,    // Vista mensual
    YEARLY,     // Vista anual
    WEEKLY,
    ALL_TIME    // Todo de todos los años
}

// Estado de la UI para Compose
data class SettingsUiState(
    val reportEmails: Set<String> = emptySet(),
    val senderEmail: String = "",
    val senderPassword: String = "", // Siempre vacío por seguridad
    val viewMode: ViewMode = ViewMode.MONTHLY,
    val isLoading: Boolean = false
)

class SettingsRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    // Keys para DataStore
    private object PreferencesKeys {
        val REPORT_EMAILS = stringSetPreferencesKey("report_emails")
        val SENDER_EMAIL = stringPreferencesKey("sender_email")
        val SENDER_PASSWORD = stringPreferencesKey("sender_password")
        val VIEW_MODE = stringPreferencesKey("view_mode")
    }

    // Clave de encriptación (en producción usa KeyStore)
    private val encryptionKey = "ut-bwhsc3JY-G51k7gTU" // 16 caracteres para AES

    // Encriptar contraseña
    private fun encrypt(password: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(password.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            // Log del error
            e.printStackTrace()
            password // Fallback sin encriptación
        }
    }

    // Desencriptar contraseña
    private fun decrypt(encryptedPassword: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(encryptedPassword, Base64.DEFAULT)
            String(cipher.doFinal(decoded))
        } catch (e: Exception) {
            // Log del error
            e.printStackTrace()
            encryptedPassword // Fallback si no se puede desencriptar
        }
    }

    // Flow para observar los settings
    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            // Manejar errores de DataStore
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                reportEmails = preferences[PreferencesKeys.REPORT_EMAILS] ?: emptySet(),
                senderEmail = preferences[PreferencesKeys.SENDER_EMAIL] ?: "",
                senderPassword = decrypt(preferences[PreferencesKeys.SENDER_PASSWORD] ?: ""),
                viewMode = try {
                    ViewMode.valueOf(preferences[PreferencesKeys.VIEW_MODE] ?: ViewMode.MONTHLY.name)
                } catch (e: Exception) {
                    ViewMode.MONTHLY
                }
            )
        }

    // Guardar todos los settings
    suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPORT_EMAILS] = settings.reportEmails
            preferences[PreferencesKeys.SENDER_EMAIL] = settings.senderEmail
            preferences[PreferencesKeys.SENDER_PASSWORD] = encrypt(settings.senderPassword)
            preferences[PreferencesKeys.VIEW_MODE] = settings.viewMode.name
        }
    }

    // Métodos individuales para emails de reporte
    suspend fun addReportEmail(email: String) {
        dataStore.edit { preferences ->
            val currentEmails = preferences[PreferencesKeys.REPORT_EMAILS] ?: emptySet()
            preferences[PreferencesKeys.REPORT_EMAILS] = currentEmails + email
        }
    }

    suspend fun removeReportEmail(email: String) {
        dataStore.edit { preferences ->
            val currentEmails = preferences[PreferencesKeys.REPORT_EMAILS] ?: emptySet()
            preferences[PreferencesKeys.REPORT_EMAILS] = currentEmails - email
        }
    }

    // Guardar credenciales del remitente
    suspend fun saveSenderCredentials(email: String, password: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SENDER_EMAIL] = email
            preferences[PreferencesKeys.SENDER_PASSWORD] = encrypt(password)
        }
    }

    // Cambiar modo de vista
    suspend fun setViewMode(viewMode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_MODE] = viewMode.name
        }
    }

    // Obtener settings actuales (para casos donde no se necesita Flow)
    suspend fun getCurrentSettings(): AppSettings {
        return dataStore.data
            .map { preferences ->
                AppSettings(
                    reportEmails = preferences[PreferencesKeys.REPORT_EMAILS] ?: emptySet(),
                    senderEmail = preferences[PreferencesKeys.SENDER_EMAIL] ?: "",
                    senderPassword = decrypt(preferences[PreferencesKeys.SENDER_PASSWORD] ?: ""),
                    viewMode = try {
                        ViewMode.valueOf(preferences[PreferencesKeys.VIEW_MODE] ?: ViewMode.MONTHLY.name)
                    } catch (e: Exception) {
                        ViewMode.MONTHLY
                    }
                )
            }
            .first()
    }
}
