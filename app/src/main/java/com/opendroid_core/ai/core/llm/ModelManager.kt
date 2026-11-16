// Modified for Open Jarvis: package namespace remapped from com.opendroid to com.opendroid_core. Copyright 2026 OpenDroid Contributors.
package com.opendroid_core.ai.core.llm

typealias OnDeviceModel = OnDeviceModelSpec

interface ModelManager {
    suspend fun download(model: OnDeviceModel)
    suspend fun delete(model: OnDeviceModel)
    suspend fun load(model: OnDeviceModel)
    suspend fun isDownloaded(model: OnDeviceModel): Boolean
    suspend fun currentModel(): OnDeviceModel?
}
