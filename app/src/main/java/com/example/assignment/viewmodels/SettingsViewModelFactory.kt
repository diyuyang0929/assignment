package com.example.assignment.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.api.RoomApi

class SettingsViewModelFactory(
    private val roomApi: RoomApi,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(roomApi, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 