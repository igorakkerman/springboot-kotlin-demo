package de.igorakkerman.demo.deviceconfig

import DeviceService
import ItemAreadyExistsException
import NoSuchItemException
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution.HD
import de.igorakkerman.demo.deviceconfig.application.Resolution.UHD
import de.igorakkerman.demo.deviceconfig.persistence.JpaConfiguration
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
@ContextConfiguration(classes = [DeviceService::class, JpaConfiguration::class])
class DeviceServiceTest(
        @Autowired
        val deviceService: DeviceService
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
        deviceService.createDevice(computer)
        deviceService.createDevice(display)

        // when
        val foundDevice = deviceService.findDeviceById(display.id)

        // then
        foundDevice shouldBe display
    }

    @Test
    fun `create Device, findDeviceById should not find by unknown id`() {

        // given
        deviceService.createDevice(computer)

        // when
        val foundDevice = deviceService.findDeviceById(display.id)

        // then
        foundDevice shouldBe null
    }

    @Test
    fun `creating two Devices with the same id should throw Exception`() {

        // given
        deviceService.createDevice(computer)

        // when/then
        shouldThrow<ItemAreadyExistsException> {
            deviceService.createDevice(display.copy(
                    id = computer.id
            ))
        }

        // then
        // exception thrown
    }

    @Test
    fun `create, then update Device, findDeviceById should return updated values`() {

        // given
        deviceService.createDevice(display)

        val newDisplay = Display(
                id = display.id,
                name = "deskscreen-0815",
                resolution = HD
        )

        // when
        deviceService.updateDevice(newDisplay)
        val foundDevice = deviceService.findDeviceById(display.id)

        // then
        foundDevice shouldBe newDisplay
    }

    @Test
    fun `update unknown device should throw Exception`() {

        // given
        // empty database

        // when/then
        shouldThrow<NoSuchItemException> {
            deviceService.updateDevice(display)
        }
    }

    @Test
    fun `create two Devices, findAllDevices should find both Devices`() {

        // given
        deviceService.createDevice(computer)
        deviceService.createDevice(display)

        // when
        val foundDevices = deviceService.findAllDevices()

        // then
        foundDevices should containExactlyInAnyOrder(computer, display)
    }

    @Test
    fun `in empty database, findAllDevices should return empty list`() {

        // given
        // empty database

        // when
        val foundDevices = deviceService.findAllDevices()

        // then
        foundDevices should beEmpty()
    }
}
