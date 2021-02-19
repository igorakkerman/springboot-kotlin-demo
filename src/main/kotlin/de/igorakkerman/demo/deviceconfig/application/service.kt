package de.igorakkerman.demo.deviceconfig.application

class DeviceService(private val dataStore: DataStore) {
    fun createDevice(device: Device) = dataStore.transactional {
        dataStore.createDevice(device)
    }

    fun findDeviceById(deviceId: DeviceId): Device? = dataStore.transactional {
        dataStore.findDeviceById(deviceId)
    }

    fun updateDevice(device: Device) = dataStore.transactional {
        dataStore.updateDevice(device)
    }

    fun findAllDevices(): List<Device> = dataStore.transactional {
        dataStore.findAllDevices()
    }
}

class NoSuchItemException(deviceId: DeviceId) : RuntimeException(deviceId)
class ItemAreadyExistsException(deviceId: DeviceId) : RuntimeException(deviceId)
