package com.bdavidgm.consumoelectrico.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val reportEmails: Set<String> = emptySet(),
    val senderEmail: String = "",
    val senderPassword: String = "", // Contraseña desencriptada
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

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {

    // Keys para DataStore (para datos no sensibles)
    private object PreferencesKeys {
        val REPORT_EMAILS = stringSetPreferencesKey("report_emails")
        val SENDER_EMAIL = stringPreferencesKey("sender_email")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        // La contraseña NO va aquí, va a EncryptedSharedPreferences
    }

    // Keys para EncryptedSharedPreferences (para datos sensibles)
    private object SecureKeys {
        const val PASSWORD_KEY = "encrypted_sender_password"
        const val ENCRYPTION_KEY_ALIAS = "app_encryption_key"
    }

    // Instancia de EncryptedSharedPreferences
    private val encryptedPrefs by lazy {
        try {
            // Crear MasterKey
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Crear EncryptedSharedPreferences
            EncryptedSharedPreferences.create(
                context,
                "secure_settings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback a SharedPreferences normales (NO RECOMENDADO para producción)
            context.getSharedPreferences("secure_settings_fallback", Context.MODE_PRIVATE).also {
                // Log de advertencia
                android.util.Log.w("SettingsRepository", "Usando SharedPreferences normales: ${e.message}")
            }
        }
    }

    // Generar clave de encriptación segura usando Android Keystore
    private fun getOrCreateEncryptionKey(): SecretKey {
        return try {
            // Intentar obtener la clave del Keystore
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(SecureKeys.ENCRYPTION_KEY_ALIAS)) {
                // Crear nueva clave
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    SecureKeys.ENCRYPTION_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }

            // Obtener la clave
            keyStore.getKey(SecureKeys.ENCRYPTION_KEY_ALIAS, null) as SecretKey
        } catch (e: Exception) {
            // Fallback: generar clave en memoria (menos seguro pero funcional)
            android.util.Log.w("SettingsRepository", "Usando clave en memoria: ${e.message}")
            generateInMemoryKey()
        }
    }

    // Generar clave en memoria como fallback
    private fun generateInMemoryKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    // Encriptar contraseña usando EncryptedSharedPreferences
    private fun encryptAndSavePassword(password: String) {
        try {
            // Guardar en EncryptedSharedPreferences
            val prefs = encryptedPrefs
            if (prefs != null) {
                prefs.edit().apply {
                    putString(SecureKeys.PASSWORD_KEY, password)
                    apply()
                }
                android.util.Log.d("SettingsRepository", "Contraseña guardada correctamente: ***${password.length} caracteres***")
            } else {
                android.util.Log.e("SettingsRepository", "ERROR: encryptedPrefs es null, no se puede guardar la contraseña")
                throw IllegalStateException("No se pudo inicializar EncryptedSharedPreferences")
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsRepository", "Error al guardar contraseña: ${e.message}", e)
            throw e
        }
    }

    // Desencriptar y obtener contraseña
    private fun getDecryptedPassword(): String {
        return try {
            // Obtener de EncryptedSharedPreferences
            val prefs = encryptedPrefs
            if (prefs != null) {
                val password = prefs.getString(SecureKeys.PASSWORD_KEY, "") ?: ""
                android.util.Log.d("SettingsRepository", "Contraseña obtenida: ${if (password.isNotEmpty()) "***${password.length} caracteres***" else "VACÍA"}")
                password
            } else {
                android.util.Log.e("SettingsRepository", "ERROR: encryptedPrefs es null, no se puede obtener la contraseña")
                ""
            }
        } catch (e: Exception) {
            android.util.Log.e("SettingsRepository", "Error al obtener contraseña: ${e.message}", e)
            ""
        }
    }

    // Flow para observar los settings (combina DataStore y EncryptedSharedPreferences)
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
                // Obtener contraseña desde EncryptedSharedPreferences
                senderPassword = getDecryptedPassword(),
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
            preferences[PreferencesKeys.VIEW_MODE] = settings.viewMode.name
        }
        // Guardar contraseña en EncryptedSharedPreferences
        encryptAndSavePassword(settings.senderPassword)
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
        }
        // Guardar contraseña en EncryptedSharedPreferences
        encryptAndSavePassword(password)
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
                    // Obtener contraseña desde EncryptedSharedPreferences
                    senderPassword = getDecryptedPassword(),
                    viewMode = try {
                        ViewMode.valueOf(preferences[PreferencesKeys.VIEW_MODE] ?: ViewMode.MONTHLY.name)
                    } catch (e: Exception) {
                        ViewMode.MONTHLY
                    }
                )
            }
            .first()
    }

    // Método para limpiar todas las configuraciones
    suspend fun clearAllSettings() {
        // Limpiar DataStore
        dataStore.edit { preferences ->
            preferences.clear()
        }
        // Limpiar EncryptedSharedPreferences
        encryptedPrefs?.edit()?.clear()?.apply()
    }

    // Método para verificar si hay contraseña guardada
    fun hasPassword(): Boolean {
        return getDecryptedPassword().isNotEmpty()
    }

    // Método para eliminar solo la contraseña
    fun clearPassword() {
        encryptedPrefs?.edit()?.remove(SecureKeys.PASSWORD_KEY)?.apply()
    }
}