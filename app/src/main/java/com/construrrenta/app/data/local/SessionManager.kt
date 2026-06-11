package com.construrrenta.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("constru_renta_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "user_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_ROLE = "user_role"
        private const val KEY_FIRST_NAME = "user_first_name"
    }

    // Guarda el token y extrae claims del JWT
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)

        // Decodificar el payload del JWT para extraer datos del usuario
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                val json = JSONObject(payload)

                editor.putString(KEY_USER_ID, json.optString("sub", ""))
                editor.putString(KEY_EMAIL, json.optString("email", ""))
                editor.putString(KEY_ROLE, json.optString("role", ""))
                editor.putString(KEY_FIRST_NAME, json.optString("firstName", ""))
            }
        } catch (_: Exception) {
            // Si el token no se puede decodificar, solo guardamos el token raw
        }

        editor.apply()
    }

    fun fetchAuthToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun getFirstName(): String? = prefs.getString(KEY_FIRST_NAME, null)

    fun isLoggedIn(): Boolean = fetchAuthToken() != null

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_ROLE)
            .remove(KEY_FIRST_NAME)
            .apply()
    }
}