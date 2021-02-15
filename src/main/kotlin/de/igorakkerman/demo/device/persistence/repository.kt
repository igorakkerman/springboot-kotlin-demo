package de.igorakkerman.demo.device.persistence

import DataStore
import de.igorakkerman.demo.device.application.Device
import de.igorakkerman.demo.device.application.DeviceId
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

interface DeviceRepository : CrudRepository<DeviceEntity, DeviceId>

@Repository
class JpaDatabase(
        private val deviceRepository: DeviceRepository
) : DataStore {
    override fun createDevice(device: Device) {
        deviceRepository.save(device.toEntity())
    }

    override fun findDeviceById(deviceId: DeviceId): Device? {
        return deviceRepository.findByIdOrNull(deviceId)?.toDevice()
    }

    override fun updateDevice(device: Device) {
        deviceRepository.save(device.toEntity())
    }

    override fun findAllDevices(): List<Device> {
        return deviceRepository.findAll().map { it.toDevice() }
    }
}
