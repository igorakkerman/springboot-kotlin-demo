package de.igorakkerman.demo.deviceconfig.persistence

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceRepository
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.sequences.shouldExist
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.validation.ConstraintViolationException

@Transactional
abstract class DeviceRepositoryTestBase(
    private val deviceRepository: DeviceRepository,
) {

    private val computer = Computer(
        id = "pc-win10-0815",
        name = "workpc-0815",
        username = "root",
        password = "topsecret",
        ipAddress = "127.0.0.1",
    )

    private val display = Display(
        id = "screen-samsung4k-4711",
        name = "workscreen-4711",
        resolution = Resolution.UHD
    )

    @Test
    fun `create two Devices, findDeviceById should find the right one`() {

        // given
        deviceRepository.createDevice(computer)
        deviceRepository.createDevice(display)
        flushAndClear()

        // when
        val foundComputer = deviceRepository.findDeviceById(computer.id)
        val foundDisplay = deviceRepository.findDeviceById(display.id)

        // then
        foundComputer shouldBe computer
        foundDisplay shouldBe display
    }

    @Test
    fun `create Device, findDeviceById should not find by unknown id`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when / then
        shouldThrow<DeviceNotFoundException> {
            deviceRepository.findDeviceById(display.id)
        }
    }

    @Test
    fun `create two Devices, findDeviceTypeById should find the type`() {

        // given
        deviceRepository.createDevice(computer)
        deviceRepository.createDevice(display)
        flushAndClear()

        // when
        val foundDisplayType = deviceRepository.findDeviceTypeById(display.id)
        val foundComputerType = deviceRepository.findDeviceTypeById(computer.id)

        // then
        foundComputerType shouldBe Computer::class
        foundDisplayType shouldBe Display::class
    }

    @Test
    fun `creating two Devices of same type with the same id should throw Exception`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when/then
        shouldThrow<DeviceAreadyExistsException> {
            deviceRepository.createDevice(
                computer.copy(
                    id = computer.id,
                    name = "different name",
                    username = "different username"
                )
            )
            flushAndClear()
        }
    }

    @Test
    fun `creating two Devices of different type with the same id should throw Exception`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when/then
        shouldThrow<DeviceAreadyExistsException> {
            deviceRepository.createDevice(
                display.copy(
                    id = computer.id
                )
            )
            flushAndClear()
        }
    }

    @Test
    fun `create, then update data of Computer, findDeviceById should return updated values`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        val computerUpdate = ComputerUpdate(
            name = "deskscreen-0815",
            ipAddress = "192.168.178.111"
        )

        // when
        deviceRepository.updateDevice(computer.id, computerUpdate)
        flushAndClear()

        // then
        val foundDevice = deviceRepository.findDeviceById(computer.id)
        foundDevice shouldBe computer.copy(
            name = computerUpdate.name!!,
            ipAddress = computerUpdate.ipAddress!!
        )
    }

    @Test
    fun `create, then update data of Display, findDeviceById should return updated values`() {

        // given
        deviceRepository.createDevice(display)
        flushAndClear()

        val displayUpdate = DisplayUpdate(
            name = "deskscreen-0815",
            resolution = Resolution.HD
        )

        // when
        deviceRepository.updateDevice(display.id, displayUpdate)
        flushAndClear()

        // then
        val foundDevice = deviceRepository.findDeviceById(display.id)
        foundDevice shouldBe display.copy(
            name = displayUpdate.name!!,
            resolution = displayUpdate.resolution!!
        )
    }

    @Test
    fun `update data in unknown device should throw Exception`() {

        // given
        // empty database

        // when/then
        shouldThrow<DeviceNotFoundException> {
            deviceRepository.updateDevice("unknown-id", DisplayUpdate())
        }
    }

    @Test
    fun `create, then replace Computer, findDeviceById should return updated values`() {

        // given
        deviceRepository.createDevice(computer)
        flushAndClear()

        // when
        val updatedComputer = computer.copy(
            name = "deskpc-0815",
            ipAddress = "34.245.198.211"
        )

        deviceRepository.replaceDevice(updatedComputer)
        flushAndClear()

        // then
        val foundDevice = deviceRepository.findDeviceById(computer.id)
        foundDevice shouldBe updatedComputer
    }

    @Test
    fun `create, then replace Display, findDeviceById should return updated values`() {

        // given
        deviceRepository.createDevice(display)
        flushAndClear()

        val updatedDisplay = display.copy(
            name = "deskscreen-0815",
            resolution = Resolution.HD
        )

        // when
        deviceRepository.replaceDevice(updatedDisplay)
        flushAndClear()

        // then
        val foundDevice = deviceRepository.findDeviceById(display.id)
        foundDevice shouldBe updatedDisplay
    }

    @Test
    fun `replace unknown device should throw Exception`() {

        // given
        // empty database

        // when/then
        shouldThrow<DeviceNotFoundException> {
            deviceRepository.replaceDevice(display.copy(id = "unknown-id"))
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
    // allow catching TransactionException from repository transaction
    // alternative: run non-transactional, that is, outside a @DataJpaTest or @Transactional-annotated class
    @Transactional(propagation = Propagation.SUPPORTS)
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

    protected abstract fun flushAndClear()
}