package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceRepository
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.ItemAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.NoSuchItemException
import de.igorakkerman.demo.deviceconfig.application.Resolution.HD
import de.igorakkerman.demo.deviceconfig.application.Resolution.UHD
import de.igorakkerman.demo.deviceconfig.persistence.JpaConfiguration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.sequences.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation.SUPPORTS
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.validation.ConstraintViolationException

@DataJpaTest
@ContextConfiguration(classes = [JpaConfiguration::class])
class DeviceRepositoryIntegrationTest(
        @Autowired
        val deviceRepository: DeviceRepository,

        @Autowired
        val entityManager: EntityManager
) {

    val computer = Computer(
            id = "pc-win10-0815",
            name = "workpc-0815",
            username = "root",
            password = "secret",
            ipAddress = "127.0.0.1",
    )

    val display = Display(
            id = "screen-samsung4k-4711",
            name = "workscreen-4711",
            resolution = UHD
    )

    @Test
    fun `create two Devices, findDeviceById should find the right one`() {

        // given
        deviceRepository.createDevice(computer)
        deviceRepository.createDevice(display)
        flushAndClear()

        // when
        val foundDevice = deviceRepository.findDeviceById(display.id)

        // then
        foundDevice shouldBe display
    }

    @Test
    fun `create Device, findDeviceById should not find by unknown id`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when
        val foundDevice = deviceRepository.findDeviceById(display.id)

        // then
        foundDevice shouldBe null
    }

    @Test
    fun `creating two Devices of same type with the same id should throw Exception`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when/then
        shouldThrow<ItemAreadyExistsException> {
            deviceRepository.createDevice(computer.copy(
                    id = computer.id,
                    name = "different name",
                    username = "different username"
            ))
            flushAndClear()
        }
    }

    @Test
    fun `creating two Devices of different type with the same id should throw Exception`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when/then
        shouldThrow<ItemAreadyExistsException> {
            deviceRepository.createDevice(display.copy(
                    id = computer.id
            ))
            flushAndClear()
        }
    }

    @Test
    fun `create, then update Device, findDeviceById should return updated values`() {

        // given
        deviceRepository.createDevice(display)
        flushAndClear()

        val displayUpdate = DisplayUpdate(
                name = "deskscreen-0815",
                resolution = HD
        )

        // when
        deviceRepository.updateDevice(display.id, displayUpdate)
        flushAndClear()

        // then
        val foundDevice = deviceRepository.findDeviceById(display.id)
        foundDevice shouldBe Display(
                id = display.id,
                name = displayUpdate.name!!,
                resolution = displayUpdate.resolution!!
        )
    }

    @Test
    fun `update unknown device should throw Exception`() {

        // given
        // empty database

        // when/then
        shouldThrow<NoSuchItemException> {
            deviceRepository.updateDevice("unknown-id", DisplayUpdate())
        }
    }

    @Test
    fun `create two Devices, findAllDevices should find both Devices`() {

        // given
        deviceRepository.createDevice(computer)
        deviceRepository.createDevice(display)
        flushAndClear()

        // when
        val foundDevices = deviceRepository.findAllDevices()

        // then
        foundDevices should containExactlyInAnyOrder(computer, display)
    }

    @Test
    fun `in empty database, findAllDevices should return empty list`() {

        // given
        // empty database

        // when
        val foundDevices = deviceRepository.findAllDevices()

        // then
        foundDevices should beEmpty()
    }

    @Test
    // allow to catch TransactionException from repository transaction
    // alternative: run non-transactional, that is, outside a @DataJpaTest or @Transactional-annotated class
    @Transactional(propagation = SUPPORTS)
    fun `ip address should have IPv4 format`() {
        // A bad ip address should not even make it to the application layer (or the persistence layer).
        // This test makes sure that if for some reason the prior level checks fail,
        // the item is not persisted and the persistence layer responds with some kind of Exception,
        // that has, at some point down the cause stack, a ConstraintViolationException.
        // The actual Exception type or the level of exceptions are implementation details and not part of the test.

        // given
        val newComputer = computer.copy(
                ipAddress = "::1"
        )

        // when/then
        val thrown: Throwable = shouldThrow<Exception> {
            deviceRepository.createDevice(newComputer)
        }

        generateSequence(thrown) { it.cause } shouldExist { it is ConstraintViolationException }
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}
