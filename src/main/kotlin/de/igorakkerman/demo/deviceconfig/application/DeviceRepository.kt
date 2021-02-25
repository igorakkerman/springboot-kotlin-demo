package de.igorakkerman.demo.deviceconfig.application

import kotlin.reflect.KClass

// Facade for persistence operations
interface DeviceRepository {
    fun <Result> transactional(function: () -> Result): Result
    fun createDevice(device: Device)
    fun findDeviceTypeById(deviceId: DeviceId): KClass<out Device>
    fun findDeviceById(deviceId: DeviceId): Device
    fun updateDevice(device: Device)
    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate)
    fun findAllDevices(): List<Device>
}
