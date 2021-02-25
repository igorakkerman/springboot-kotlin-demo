package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class UpdateDevicePartiallyControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"
    private val computerUpdateFull = ComputerUpdate(name = "best mac", username = "timapple", password = "0n3m0r3th1ng", ipAddress = "192.168.178.1")
    private val computerUpdatePartial = ComputerUpdate(name = "second best mac", ipAddress = "127.0.0.1")
    private val displayId = "samsung-screen-88276"
    private val displayUpdateFull = DisplayUpdate("second best screen", resolution = WQHD)

    @Test
    fun `update computer with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdateFull.name}",
                    "username": "${computerUpdateFull.username}",
                    "password": "${computerUpdateFull.password}",
                    "ipAddress": "${computerUpdateFull.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(computerId, computerUpdateFull) }
    }

    @Test
    fun `update display with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${displayUpdateFull.name}",
                    "resolution": "${displayUpdateFull.resolution}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(displayId, displayUpdateFull) }
    }

    @Test
    fun `update computer with partial valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdatePartial.name}",
                    "ipAddress": "${computerUpdatePartial.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(computerId, computerUpdatePartial) }
    }

    @Test
    fun `update device with unknown fields should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            // 'id' is not a valid updatable field, it is part of the URL
            content = """
                {
                    "id": "${displayId}",
                    "name": "${displayUpdateFull.name}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `update display with computer data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdateFull.name}",
                    "username": "${computerUpdateFull.username}",
                    "password": "${computerUpdateFull.password}",
                    "ipAddress": "${computerUpdateFull.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `update unknown device should lead to response 404 not found`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } throws DeviceNotFoundException(computerId)

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdateFull.name}",
                }
            """
        }.andExpect {
            status { isNotFound() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }
}
