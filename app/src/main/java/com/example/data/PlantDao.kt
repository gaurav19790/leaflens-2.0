package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY id DESC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantById(id: Int): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE name = :name LIMIT 1")
    fun getPlantByName(name: String): Flow<Plant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlantById(id: Int)
}
