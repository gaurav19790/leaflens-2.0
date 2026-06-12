package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ScanHistoryItem(
    val id: String,
    val plantName: String,
    val species: String,
    val healthStatus: String,
    val disease: String?,
    val severityLevel: String?,
    val timestamp: Long
)

class ScanHistoryViewModel : ViewModel() {
    private val auth by lazy { 
        try { FirebaseAuth.getInstance() } catch(e: Exception) { null } 
    }
    private val firestore by lazy { 
        try { FirebaseFirestore.getInstance() } catch(e: Exception) { null } 
    }

    private val _history = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val history: StateFlow<List<ScanHistoryItem>> = _history.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchScanHistory()
    }

    fun fetchScanHistory() {
        if (auth?.currentUser == null || firestore == null) return
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = auth?.currentUser?.uid ?: return@launch
                val snapshot = firestore?.collection("users")?.document(userId)?.collection("scan_history")
                    ?.orderBy("timestamp", Query.Direction.DESCENDING)
                    ?.get()?.await()
                
                val fetched = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("plantName") ?: return@mapNotNull null
                    val species = doc.getString("species") ?: ""
                    val currentHealth = doc.getString("healthStatus") ?: "Unknown"
                    val disease = doc.getString("disease")
                    val severity = doc.getString("severityLevel")
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    
                    ScanHistoryItem(
                        id = doc.id,
                        plantName = name,
                        species = species,
                        healthStatus = currentHealth,
                        disease = disease,
                        severityLevel = severity,
                        timestamp = timestamp
                    )
                } ?: emptyList()
                
                _history.value = fetched
            } catch (e: Exception) {
                // Ignore or handle
            } finally {
                _isLoading.value = false
            }
        }
    }
}
