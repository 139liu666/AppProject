package com.peizhe.todo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "User_Pres")
class UserPreference(private val context: Context) {
    companion object{
        val SESSION_ID = stringPreferencesKey("session_id")
        val ACCOUNT_ID = intPreferencesKey("account_id")
    }
    val userData: Flow<Pair<String?, Int>> = context.dataStore.data
        .map { preferences ->
            val sessionId = preferences[SESSION_ID]
            val accountId = preferences[ACCOUNT_ID] ?: 0
            Pair(sessionId, accountId)
        }
    suspend fun saveUser(sessionId: String, accountId: Int) {
        context.dataStore.edit { preferences ->
            preferences[SESSION_ID] = sessionId
            preferences[ACCOUNT_ID] = accountId
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}