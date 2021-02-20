package de.igorakkerman.demo.deviceconfig.application

class DeviceService(private val deviceStore: DeviceStore) {
    fun createDevice(device: Device) = deviceStore.transactional {
        deviceStore.createDevice(device)
    }

    fun findDeviceById(deviceId: DeviceId): Device? = deviceStore.transactional {
        deviceStore.findDeviceById(deviceId)
    }

    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) = deviceStore.transactional {
        deviceStore.updateDevice(deviceId, deviceUpdate)
    }

    fun findAllDevices(): List<Device> = deviceStore.transactional {
        deviceStore.findAllDevices()
    }
}

class NoSuchItemException(deviceId: DeviceId) : RuntimeException(deviceId)
class ItemAreadyExistsException(deviceId: DeviceId) : RuntimeException(deviceId)
