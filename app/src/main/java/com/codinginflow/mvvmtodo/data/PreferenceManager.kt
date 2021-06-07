package com.codinginflow.mvvmtodo.data

import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder{
    NAME,DATE
}

data class FilterPreference(val sortOrder: SortOrder,val hideComplted : Boolean)

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context : Context) {
    private val dataStore = context.createDataStore("user_preferences")

    val preferences = dataStore.data
            .catch { exception ->
                if(exception is IOException){
                    emit(emptyPreferences())
                }else{
                    throw exception
                }
            }
            .map {
                val sortOrder = SortOrder.valueOf(
                        it[preferencesKeys.SORT_ORDER]?:SortOrder.DATE.name)
                val hideCompleted = it[preferencesKeys.HIDE_COMPLETED]?: false
                FilterPreference(sortOrder,hideCompleted)
            }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit { preferences->
            preferences[preferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }
    suspend fun updateHideCompleted(hideCompleted : Boolean){
        dataStore.edit { preferences->
            preferences[preferencesKeys.HIDE_COMPLETED] = hideCompleted

        }
    }

    private object preferencesKeys{
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }
}