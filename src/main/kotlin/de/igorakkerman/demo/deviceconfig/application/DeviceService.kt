package de.igorakkerman.demo.deviceconfig.application

import kotlin.reflect.KClass

class DeviceService(private val deviceRepository: DeviceRepository) {
    fun createDevice(device: Device) = deviceRepository.transactional {
        deviceRepository.createDevice(device)
    }

    fun findDeviceById(deviceId: DeviceId): Device? = deviceRepository.transactional {
        deviceRepository.findDeviceById(deviceId)
    }

    fun findDeviceTypeById(deviceId: DeviceId): KClass<out Device> = deviceRepository.transactional {
        deviceRepository.findDeviceTypeById(deviceId)
    }

    val c: KClass<Device> = Device::class

    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) = deviceRepository.transactional {
        deviceRepository.updateDevice(deviceId, deviceUpdate)
    }

    fun findAllDevices(): List<Device> = deviceRepository.transactional {
        deviceRepository.findAllDevices()
    }
}

class NoSuchDeviceException(deviceId: DeviceId) : RuntimeException("No such device. deviceId=$deviceId")
class DeviceAreadyExistsException(val deviceId: DeviceId) : RuntimeException("Device already exists. deviceId=$deviceId")
