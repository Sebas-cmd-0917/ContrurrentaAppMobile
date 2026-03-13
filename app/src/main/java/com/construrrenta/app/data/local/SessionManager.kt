package com.construrrenta.app.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Archivo local seguro para guardar datos de sesión
    private val prefs: SharedPreferences = context.getSharedPreferences("constru_renta_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }

    // Guarda el token tras un login exitoso
    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    // Recupera el token para inyectarlo en las peticiones
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Limpia la sesión al cerrar cuenta
    fun clearSession() {
        prefs.edit().remove(USER_TOKEN).apply()
    }
}