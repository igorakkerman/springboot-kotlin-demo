package de.igorakkerman.demo.deviceconfig.application

import mu.KotlinLogging
import kotlin.reflect.KClass

class DeviceService(private val deviceRepository: DeviceRepository) {

    private val log = KotlinLogging.logger {}

    fun findDeviceById(deviceId: DeviceId): Device = deviceRepository.transactional {
        log.info("Finding device. deviceId: $deviceId")

        deviceRepository.findDeviceById(deviceId)
            .also { log.info("Device found. deviceId: $deviceId, device: $it") }
    }

    fun findDeviceTypeById(deviceId: DeviceId): KClass<out Device> = deviceRepository.transactional {
        log.info("Finding device type. deviceId: $deviceId")

        deviceRepository.findDeviceTypeById(deviceId)
            .also { log.info("Device type found. deviceId: $deviceId, deviceType: ${it.simpleName}") }
    }

    fun findAllDevices(): List<Device> = deviceRepository.transactional {
        log.info("Finding all devices.")

        deviceRepository.findAllDevices()
            .also { log.info { "Devices found. devices: $it" } }
    }

    fun createDevice(device: Device) = deviceRepository.transactional {
        log.info("Creating device.")

        deviceRepository.createDevice(device)
            .also { log.info("Device created. device: $it") }
    }

    fun replaceDevice(device: Device) = deviceRepository.transactional {
        log.info("Replacing device. deviceId: $device.id, device: $device")

        val existingDeviceType: KClass<out Device> = deviceRepository.findDeviceTypeById(device.id)

        log.debug("Device exists. deviceId: ${device.id}, deviceType: ${existingDeviceType.simpleName}")

        if (device::class != existingDeviceType)
            throw DeviceTypeConflictException(deviceId = device.id, existingDeviceType, device::class)

        deviceRepository.replaceDevice(device)
            .also { log.info("Device replaced. deviceId: $device.id.") }
    }

    fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) = deviceRepository.transactional {
        log.info("Updating device. deviceId: $deviceId, deviceUpdate: $deviceUpdate")

        deviceRepository.updateDevice(deviceId, deviceUpdate)
            .also { log.info("Device updated. deviceId: $deviceId.") }
    }
}

class DeviceNotFoundException(val deviceId: DeviceId) : RuntimeException("No such device. deviceId=$deviceId")
class DeviceAreadyExistsException(val deviceId: DeviceId) : RuntimeException("Device already exists. deviceId=$deviceId")
class DeviceTypeConflictException(val deviceId: DeviceId, val existingDeviceType: KClass<out Device>, val invalidDeviceType: KClass<out Device>) :
    RuntimeException("Types of existing and specified device don't match. deviceId: $deviceId, existingDeviceType: $existingDeviceType, invalidDeviceType: $invalidDeviceType")