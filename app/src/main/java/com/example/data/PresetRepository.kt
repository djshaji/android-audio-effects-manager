package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PresetRepository(private val presetDao: PresetDao) {

    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun getPresetById(id: Long): PresetEntity? = withContext(Dispatchers.IO) {
        presetDao.getPresetById(id)
    }

    suspend fun savePreset(preset: PresetEntity): Long = withContext(Dispatchers.IO) {
        presetDao.insertPreset(preset)
    }

    suspend fun updatePreset(preset: PresetEntity) = withContext(Dispatchers.IO) {
        presetDao.updatePreset(preset)
    }

    suspend fun deleteCustomPreset(id: Long) = withContext(Dispatchers.IO) {
        presetDao.deleteCustomPresetById(id)
    }

    suspend fun ensureDefaultPresets() = withContext(Dispatchers.IO) {
        if (presetDao.getCount() == 0) {
            presetDao.insertAll(DefaultPresets.FACTORY_PRESETS)
        }
    }
}
