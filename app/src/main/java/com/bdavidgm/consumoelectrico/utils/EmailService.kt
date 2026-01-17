package com.bdavidgm.consumoelectrico.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeBodyPart

object EmailService {
    
    fun enviarCorreo(
        usuario: String,
        password: String,
        destinatario: String,
        asunto: String,
        cuerpo: String,
        context: Context,
        scope: CoroutineScope
    ) {
        val nautaProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "false")
            put("mail.smtp.host", "smtp.nauta.cu")
            put("mail.smtp.port", "25")
            put("mail.smtp.connectiontimeout", "10000000")
            put("mail.smtp.timeout", "10000000")
        }

        val gmailProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
           // put("mail.smtp.port", "25")
            put("mail.smtp.ssl.trust", "smtp.gmail.com")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
            // Propiedades adicionales para mejor compatibilidad
            put("mail.smtp.ssl.checkserveridentity", "true")
            put("mail.smtp.ssl.enable", "false") // STARTTLS usa esto en false
        }
        
        // Detectar tipo de correo: Nauta o Gmail (cualquier dominio que no sea nauta.cu se trata como Gmail)
        val isNauta = usuario.lowercase().endsWith("@nauta.cu")
        val props = if (isNauta) nautaProps else gmailProps
        
        // Log de configuración usada
        Log.d("EmailService", "Configuración SMTP:")
        Log.d("EmailService", "  Host: ${props.getProperty("mail.smtp.host")}")
        Log.d("EmailService", "  Port: ${props.getProperty("mail.smtp.port")}")
        Log.d("EmailService", "  STARTTLS: ${props.getProperty("mail.smtp.starttls.enable")}")
        Log.d("EmailService", "  Auth: ${props.getProperty("mail.smtp.auth")}")

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                Log.d("EmailService", "Autenticando con usuario: $usuario")
                return PasswordAuthentication(usuario, password)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(usuario))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
            subject = asunto
            setText(cuerpo)
        }

        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e("EmailService", "Excepción en corrutina: ${throwable.message}", throwable)
        }

        scope.launch(Dispatchers.IO + handler) {
            try {
                Log.d("EmailService", "Intentando enviar correo desde: $usuario")
                Log.d("EmailService", "Destinatario: $destinatario")
                Log.d("EmailService", "Usando configuración: ${if (usuario.endsWith("@nauta.cu")) "Nauta" else "Gmail"}")
                
                Transport.send(message)
                Log.i("EmailService", "Correo enviado correctamente")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Correo enviado correctamente", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EmailService", "Error enviando correo", e)
                Log.e("EmailService", "Tipo de error: ${e.javaClass.simpleName}")
                Log.e("EmailService", "Mensaje: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = when {
                        e.message?.contains("535", ignoreCase = true) == true -> 
                            "Error: Credenciales incorrectas. Verifica usuario y contraseña."
                        e.message?.contains("534", ignoreCase = true) == true -> 
                            "Error: Autenticación fallida. Para Gmail, usa una Contraseña de aplicación."
                        e.message?.contains("535-5.7.8", ignoreCase = true) == true -> 
                            "Error: Gmail requiere una Contraseña de aplicación. Ve a tu cuenta de Google > Seguridad > Contraseñas de aplicaciones."
                        else -> "Error: ${e.message}"
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Envía un correo electrónico con contenido HTML
     */
    fun enviarCorreoHTML(
        usuario: String,
        password: String,
        destinatario: String,
        asunto: String,
        cuerpoHTML: String,
        context: Context,
        scope: CoroutineScope
    ) {
        val nautaProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "false")
            put("mail.smtp.host", "smtp.nauta.cu")
            put("mail.smtp.port", "25")
            put("mail.smtp.connectiontimeout", "10000000")
            put("mail.smtp.timeout", "10000000")
        }

        val gmailProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.host", "smtp.gmail.com")
           // put("mail.smtp.port", "587")
            put("mail.smtp.port", "25")
            put("mail.smtp.ssl.trust", "smtp.gmail.com")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
            put("mail.smtp.ssl.checkserveridentity", "true")
            put("mail.smtp.ssl.enable", "false")
        }
        
        val isNauta = usuario.lowercase().endsWith("@nauta.cu")
        val props = if (isNauta) nautaProps else gmailProps
        
        Log.d("EmailService", "Configuración SMTP (HTML):")
        Log.d("EmailService", "  Host: ${props.getProperty("mail.smtp.host")}")
        Log.d("EmailService", "  Port: ${props.getProperty("mail.smtp.port")}")

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                Log.d("EmailService", "Autenticando con usuario: $usuario")
                return PasswordAuthentication(usuario, password)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(usuario))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario))
            subject = asunto
            
            // Crear contenido multipart con HTML
            val multipart = MimeMultipart("alternative")
            
            // Versión texto plano (fallback para clientes que no soportan HTML)
            val textPart = MimeBodyPart().apply {
                setText("Este correo contiene un reporte en formato HTML. Por favor, use un cliente de correo que soporte HTML para ver el contenido completo.", "utf-8")
            }
            multipart.addBodyPart(textPart)
            
            // Versión HTML
            val htmlPart = MimeBodyPart().apply {
                setContent(cuerpoHTML, "text/html; charset=utf-8")
            }
            multipart.addBodyPart(htmlPart)
            
            setContent(multipart)
        }

        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e("EmailService", "Excepción en corrutina: ${throwable.message}", throwable)
        }

        scope.launch(Dispatchers.IO + handler) {
            try {
                Log.d("EmailService", "Intentando enviar correo HTML desde: $usuario")
                Log.d("EmailService", "Destinatario: $destinatario")
                Log.d("EmailService", "Usando configuración: ${if (isNauta) "Nauta" else "Gmail"}")
                
                Transport.send(message)
                Log.i("EmailService", "Correo HTML enviado correctamente")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Correo enviado correctamente", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("EmailService", "Error enviando correo HTML", e)
                Log.e("EmailService", "Tipo de error: ${e.javaClass.simpleName}")
                Log.e("EmailService", "Mensaje: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val errorMsg = when {
                        e.message?.contains("535", ignoreCase = true) == true -> 
                            "Error: Credenciales incorrectas. Verifica usuario y contraseña."
                        e.message?.contains("534", ignoreCase = true) == true -> 
                            "Error: Autenticación fallida. Para Gmail, usa una Contraseña de aplicación."
                        e.message?.contains("535-5.7.8", ignoreCase = true) == true -> 
                            "Error: Gmail requiere una Contraseña de aplicación. Ve a tu cuenta de Google > Seguridad > Contraseñas de aplicaciones."
                        else -> "Error: ${e.message}"
                    }
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
