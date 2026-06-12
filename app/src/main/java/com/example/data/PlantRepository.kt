package com.example.data

import kotlinx.coroutines.flow.Flow

class PlantRepository(private val plantDao: PlantDao) {
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun getPlantById(id: Int): Flow<Plant?> {
        return plantDao.getPlantById(id)
    }

    fun getPlantByName(name: String): Flow<Plant?> {
        return plantDao.getPlantByName(name)
    }

    suspend fun insert(plant: Plant): Long {
        return plantDao.insertPlant(plant)
    }

    suspend fun update(plant: Plant) {
        plantDao.updatePlant(plant)
    }

    suspend fun delete(plant: Plant) {
        plantDao.deletePlant(plant)
    }

    suspend fun deleteById(id: Int) {
        plantDao.deletePlantById(id)
    }
}
