package de.igorakkerman.demo.deviceconfig.application

import mu.KotlinLogging

class DeviceService(private val deviceRepository: DeviceRepository) {

    private val log = KotlinLogging.logger {}

    fun findDeviceById(deviceId: DeviceId): Device = deviceRepository.transactional {
        log.info("Finding device. deviceId: $deviceId")

        deviceRepository.findDeviceById(deviceId)
            .also { log.info("Device found. deviceId: $deviceId, device: $it") }
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
        log.info("Replacing device. deviceId: ${device.id}, device: $device")

        val existingDeviceType: DeviceType = deviceRepository.findDeviceTypeById(device.id)

        log.debug("Device exists. deviceId: ${device.id}, deviceType: ${existingDeviceType.simpleName}")

        if (device::class != existingDeviceType)
            throw DeviceTypeConflictException(deviceId = device.id, existingDeviceType, device::class)

        deviceRepository.replaceDevice(device)
            .also { log.info("Device replaced. deviceId: ${device.id}.") }
    }

    fun updateDevice(deviceId: DeviceId, deviceUpdateFor: (DeviceType) -> DeviceUpdate) = deviceRepository.transactional {
        log.info("Updating device. deviceId: $deviceId")

        val deviceType: DeviceType = deviceRepository.findDeviceTypeById(deviceId)

        log.debug("Device exists. deviceId: $deviceId, deviceType: ${deviceType.simpleName}")

        deviceRepository.updateDevice(deviceId, deviceUpdateFor(deviceType))
            .also { log.info("Device updated. deviceId: $deviceId.") }
    }
}

class DeviceNotFoundException(val deviceId: DeviceId) : RuntimeException()
class DeviceAreadyExistsException(val deviceId: DeviceId) : RuntimeException()
class DeviceTypeConflictException(val deviceId: DeviceId, val existingDeviceType: DeviceType, val invalidDeviceType: DeviceType) : RuntimeException()
