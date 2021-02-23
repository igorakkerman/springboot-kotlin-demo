package de.igorakkerman.demo.deviceconfig.application

class DeviceService(private val deviceRepository: DeviceRepository) {
    fun createDevice(device: Device) = deviceRepository.transactional {
        deviceRepository.createDevice(device)
    }

    fun findDeviceById(deviceId: DeviceId): Device? = deviceRepository.transactional {
        deviceRepository.findDeviceById(deviceId)
    }

    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) = deviceRepository.transactional {
        deviceRepository.updateDevice(deviceId, deviceUpdate)
    }

    fun findAllDevices(): List<Device> = deviceRepository.transactional {
        deviceRepository.findAllDevices()
    }
}

class NoSuchDeviceException(deviceId: DeviceId) : RuntimeException(deviceId)
class DeviceAreadyExistsException(deviceId: DeviceId) : RuntimeException(deviceId)
