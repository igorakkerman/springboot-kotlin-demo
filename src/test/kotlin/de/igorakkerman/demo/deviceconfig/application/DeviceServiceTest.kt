package de.igorakkerman.demo.deviceconfig.application

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DeviceServiceTest {
    private val computerId = "macpro-m1-95014"
    private val computer = Computer(id = computerId, name = "best mac", username = "timapple", password = "0n3m0r3th1ng", ipAddress = "192.168.178.1")
    private val displayId = "samsung-screen-88276"
    private val display = Display(id = displayId, name = "second best screen", resolution = Resolution.WQHD)

    private val repo = mockk<DeviceRepository>(relaxUnitFun = true)
    private val service = DeviceService(repo)

    @BeforeEach
    fun `mock transaction boundary`() {
        every<Unit> { repo.transactional(captureLambda()) } answers { lambda<() -> Unit>().invoke() }
    }

    @AfterEach
    fun `verify transaction was initiated once and verification is exhaustive`() {
        verify { repo.transactional(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `replace computer with full valid data should be accepted`() {
        // given
        every { repo.findDeviceTypeById(computerId) } returns Computer::class

        // when
        service.replaceDevice(computer)

        // then
        verify { repo.findDeviceTypeById(computerId) }
        verify { repo.replaceDevice(computer) }
    }

    @Test
    fun `replace display with full valid data should be accepted`() {
        // given
        every { repo.findDeviceTypeById(displayId) } returns Display::class

        // when
        service.replaceDevice(display)

        // then
        verify { repo.findDeviceTypeById(displayId) }
        verify { repo.replaceDevice(display) }
    }

    @Test
    fun `replace device with the wrong type specified in the document should lead to DeviceTypeConflictException`() {
        // given
        every { repo.findDeviceTypeById(computerId) } returns Display::class

        // when/then
        shouldThrow<DeviceTypeConflictException> {
            service.replaceDevice(computer)
        }.run {
            deviceId shouldBe computerId
            invalidDeviceType shouldBe Computer::class
            existingDeviceType shouldBe Display::class
        }

        verify { repo.findDeviceTypeById(computerId) }
    }

    @Test
    fun `replace unknown device should lead to DeviceNotFoundException`() {
        // given
        every { repo.findDeviceTypeById(computerId) } throws DeviceNotFoundException(computerId)

        // when/then
        shouldThrow<DeviceNotFoundException> {
            service.replaceDevice(computer)
        }.run {
            deviceId shouldBe computerId
        }

        verify { repo.findDeviceTypeById(computerId) }
    }
}
