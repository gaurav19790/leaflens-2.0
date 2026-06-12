package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Plant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CollectionViewModel : ViewModel() {
    private val auth by lazy { 
        try { FirebaseAuth.getInstance() } catch(e: Exception) { null } 
    }
    private val firestore by lazy { 
        try { FirebaseFirestore.getInstance() } catch(e: Exception) { null } 
    }

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchPlantsFromFirestore()
    }

    fun fetchPlantsFromFirestore() {
        if (auth?.currentUser == null || firestore == null) return
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userId = auth?.currentUser?.uid ?: return@launch
                val snapshot = firestore?.collection("users")?.document(userId)?.collection("plants")?.get()?.await()
                
                val fetched = snapshot?.documents?.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val species = doc.getString("species") ?: ""
                    val currentHealth = doc.getString("healthStatus") ?: "Unknown"
                    val healthScore = doc.getLong("healthScore")?.toInt() ?: 0
                    Plant(
                        name = name, 
                        species = species, 
                        healthStatus = currentHealth, 
                        healthScore = healthScore,
                        wateringLevel = doc.getString("wateringLevel") ?: "Medium",
                        wateringScore = 50,
                        sunlight = doc.getString("sunlight") ?: "Indirect",
                        sunlightScore = 50,
                        nextWateringTimeMs = System.currentTimeMillis() + 86400000L,
                        nextFeedingTimeMs = System.currentTimeMillis() + 86400000L * 7,
                        firestoreId = doc.id
                    )
                } ?: emptyList()
                
                _plants.value = fetched
            } catch (e: Exception) {
                // Return empty or fallback
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePlant(plant: Plant) {
        val firestoreId = plant.firestoreId ?: return
        val userId = auth?.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                firestore?.collection("users")?.document(userId)
                    ?.collection("plants")?.document(firestoreId)?.delete()?.await()
                
                fetchPlantsFromFirestore() // refresh
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
