package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
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
import org.springframework.test.web.servlet.put

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class UpdateDeviceFullControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"
    private val computer = Computer(id = computerId, name = "best mac", username = "timapple", password = "0n3m0r3th1ng", ipAddress = "192.168.178.1")
    private val displayId = "samsung-screen-88276"
    private val display = Display(id = displayId, name = "second best screen", resolution = WQHD)

    @Test
    fun `update computer with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computer.id}",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(computer) }
    }

    @Test
    fun `update display with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${display.id}",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(display) }
    }

    @Test
    fun `update computer with partial data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            // id required
            content = """
                {
                    "name": "${computer.name}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(computer) }
    }

    @Test
    fun `update device with different IDs in the URL and the document should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            // 'sizeInInch' is not a valid field
            content = """
                {
                    "id": "${computerId}",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any()) }
    }

    @Test
    fun `update device with unknown fields should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            // 'sizeInInch' is not a valid field
            content = """
                {
                    "id": "${displayId}",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}",
                    "sizeInInch": "19''" 
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any()) }
    }

    @Test
    fun `update display with computer data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${displayId}",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
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
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computer.name}",
                }
            """
        }.andExpect {
            status { isNotFound() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }
}
