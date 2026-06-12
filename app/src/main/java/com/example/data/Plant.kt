package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val species: String,
    val healthStatus: String, // e.g., "Healthy", "Alert", "Powdery Mildew"
    val healthScore: Int, // 0-100
    val wateringLevel: String, // e.g., "High", "Medium", "Low"
    val wateringScore: Int, // 0-100
    val sunlight: String, // e.g., "Bright Direct", "Indirect"
    val sunlightScore: Int, // 0-100
    val nextWateringTimeMs: Long,
    val nextFeedingTimeMs: Long,
    val disease: String? = null,
    val severityLevel: String? = null,
    val symptoms: String = "", // Comma-separated or serialized
    val treatmentSteps: String = "", // Comma-separated or serialized
    val imageUri: String? = null,
    val description: String = "",
    val similarImageUris: String = "", // Comma separated list of images
    val isPremium: Boolean = false, // Demo purpose from UI map
    val firestoreId: String? = null
)
