package de.igorakkerman.demo.deviceconfig.presistence.springdatajpa

import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceRepository
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.PersistenceContext
import kotlin.reflect.KClass

@Configuration
@EnableJpaRepositories
@EntityScan
class JpaConfiguration {
    @Bean
    @PersistenceContext
    fun deviceRepository(backingRepository: BackingRepository) = JpaDeviceRepository(backingRepository)
}

interface BackingRepository : CrudRepository<DeviceEntity, DeviceId>

@Repository
class JpaDeviceRepository(
    private val repo: BackingRepository,
) : DeviceRepository {

    @Transactional
    override fun <Result> transactional(function: () -> Result): Result = function()

    override fun createDevice(device: Device) {
        if (repo.existsById(device.id))
            throw DeviceAreadyExistsException(device.id)
        repo.save(device.toEntity())
    }

    override fun findDeviceById(deviceId: DeviceId): Device =
        repo.findByIdOrNull(deviceId)
            ?.toDevice()
            ?: throw DeviceNotFoundException(deviceId)

    override fun findDeviceTypeById(deviceId: DeviceId): KClass<out Device> =
        repo.findByIdOrNull(deviceId)?.deviceType()
            ?: throw DeviceNotFoundException(deviceId)

    override fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) {
        val deviceEntity = repo.findByIdOrNull(deviceId)
            ?: throw DeviceNotFoundException(deviceId)
        deviceUpdate.updateEntity(deviceEntity)
    }

    override fun findAllDevices(): List<Device> =
        repo.findAll().map { it.toDevice() }
}
