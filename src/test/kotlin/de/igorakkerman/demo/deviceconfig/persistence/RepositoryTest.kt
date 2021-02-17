package de.igorakkerman.demo.deviceconfig.persistence

import DataStore
import ItemAreadyExistsException
import NoSuchItemException
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution.HD
import de.igorakkerman.demo.deviceconfig.application.Resolution.UHD
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.containExactlyInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration

@DataJpaTest
@ContextConfiguration(classes = [JpaConfiguration::class])
class RepositoryTest(
        @Autowired
        val dataStore: DataStore
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
        dataStore.createDevice(computer)
        dataStore.createDevice(display)

        // when
        val foundDevice = dataStore.findDeviceById(display.id)

        // then
        foundDevice shouldBe display
    }

    @Test
    fun `create Device, findDeviceById should not find by unknown id`() {

        // given
        dataStore.createDevice(computer)

        // when
        val foundDevice = dataStore.findDeviceById(display.id)

        // then
        foundDevice shouldBe null
    }

    @Test
    fun `creating two Devices with the same id should throw Exception`() {

        // given
        dataStore.createDevice(computer)

        // when/then
        shouldThrow<ItemAreadyExistsException> {
            dataStore.createDevice(
                    Display(
                            id = computer.id,
                            name = display.name,
                            resolution = display.resolution,
                    ))
        }

        // then
        // exception thrown
    }

    @Test
    fun `create, then update Device, findDeviceById should return updated values`() {

        // given
        dataStore.createDevice(display)

        val newDisplay = Display(
                id = display.id,
                name = "deskscreen-0815",
                resolution = HD
        )

        // when
        dataStore.updateDevice(newDisplay)
        val foundDevice = dataStore.findDeviceById(display.id)

        // then
        foundDevice shouldBe newDisplay
    }

    @Test
    fun `update unknown device should throw Exception`() {

        // given
        // empty database

        // when/then
        shouldThrow<NoSuchItemException> {
            dataStore.updateDevice(display)
        }
    }

    @Test
    fun `create two Devices, findAllDevices should find both Devices`() {

        // given
        dataStore.createDevice(computer)
        dataStore.createDevice(display)

        // when
        val foundDevices = dataStore.findAllDevices()

        // then
        foundDevices should containExactlyInAnyOrder(computer, display)
    }

    @Test
    fun `in empty database, findAllDevices should return empty list`() {

        // given
        // empty database

        // when
        val foundDevices = dataStore.findAllDevices()

        // then
        foundDevices should beEmpty()
    }
}
