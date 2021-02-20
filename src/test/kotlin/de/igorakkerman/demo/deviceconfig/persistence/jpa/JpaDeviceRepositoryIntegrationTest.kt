package de.igorakkerman.demo.deviceconfig.persistence.jpa

import de.igorakkerman.demo.deviceconfig.application.DeviceRepository
import de.igorakkerman.demo.deviceconfig.persistence.DeviceRepositoryIntegrationTestBase
import de.igorakkerman.demo.deviceconfig.presistence.jpa.JpaConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import javax.persistence.EntityManager

@DataJpaTest
@ContextConfiguration(classes = [JpaConfiguration::class])
class JpaDeviceRepositoryIntegrationTest(
        @Autowired
        private val deviceRepository: DeviceRepository,

        @Autowired
        private val entityManager: EntityManager
): DeviceRepositoryIntegrationTestBase(deviceRepository) {

    override fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}
