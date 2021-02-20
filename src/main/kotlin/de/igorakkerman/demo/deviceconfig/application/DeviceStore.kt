package de.igorakkerman.demo.deviceconfig.application

// Facade for persistence operations
interface DeviceStore {
    fun <Result> transactional(function: () -> Result): Result
    fun createDevice(device: Device)
    fun findDeviceById(deviceId: DeviceId): Device?
    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate)
    fun findAllDevices(): List<Device>
}
