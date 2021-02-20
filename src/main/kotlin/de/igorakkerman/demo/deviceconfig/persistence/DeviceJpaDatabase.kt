package de.igorakkerman.demo.deviceconfig.persistence

import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceStore
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.ItemAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.NoSuchItemException
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Configuration
@EnableJpaRepositories
@EntityScan
class JpaConfiguration {
    @Bean
    @PersistenceContext
    fun deviceStore(deviceRepository: DeviceRepository) = DeviceJpaDatabase(deviceRepository)
}

interface DeviceRepository : CrudRepository<DeviceEntity, DeviceId>

@Repository
class DeviceJpaDatabase(
        private val repo: DeviceRepository,
) : DeviceStore {

    @Transactional
    override fun <Result> transactional(function: () -> Result): Result = function()

    override fun createDevice(device: Device) {
        if (repo.existsById(device.id))
            throw ItemAreadyExistsException(device.id)
        repo.save(device.toEntity())
    }

    override fun findDeviceById(deviceId: DeviceId): Device? =
            repo.findByIdOrNull(deviceId)?.toDevice()

    override fun updateDevice(deviceId: DeviceId, deviceUpdate: DeviceUpdate) {
        val deviceEntity = repo.findByIdOrNull(deviceId) ?: throw NoSuchItemException(deviceId)
        deviceUpdate.updateEntity(deviceEntity)
    }

    override fun findAllDevices(): List<Device> =
            repo.findAll().map { it.toDevice() }
}
