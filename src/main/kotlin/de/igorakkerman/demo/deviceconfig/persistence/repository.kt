package de.igorakkerman.demo.deviceconfig.persistence

import DataStore
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


@Configuration
@EnableJpaRepositories
@EntityScan
@EnableTransactionManagement
class JpaConfiguration {
    @Bean
    @PersistenceContext
    fun dataStore(deviceRepository: DeviceRepository, entityManager: EntityManager) = JpaDatabase(deviceRepository, entityManager)
}

interface DeviceRepository : CrudRepository<DeviceEntity, DeviceId>

@Repository
class JpaDatabase(
        private val deviceRepository: DeviceRepository,
        private val entityManager: EntityManager
) : DataStore {
    @Transactional
    override fun createDevice(device: Device) {
        entityManager.persist(device.toEntity())
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
