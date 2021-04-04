package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.ninjasquad.springmockk.MockkBean
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import io.mockk.Called
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
class ReplaceDeviceControllerTest(
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
    fun `replace computer with full valid data should lead to response 204 no content`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${computer.id}",
                    "type": "computer",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.replaceDevice(computer) }
    }

    @Test
    fun `replace display with full valid data should lead to response 204 no content`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${display.id}",
                    "type": "display",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}"
                }
            """
        }.andExpect {
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.replaceDevice(display) }
    }

    @Test
    fun `replace device with partial data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "type": "computer",
                    "name": "${computer.name}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
            // id required
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify(exactly = 0) { deviceService.replaceDevice(computer) }
    }

    @Test
    fun `replace device with forbidden null value should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${display.id}",
                    "type": "computer",
                    "name": null,
                    "ipAddress": "${computer.ipAddress}"
                }
            """
            // id required
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify { deviceService wasNot Called }
    }

    @Test
    fun `replace device with forbidden null id should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": null,
                    "type": "computer",
                    "name": "${computer.name}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
            // id required
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify { deviceService wasNot Called }
    }

    @Test
    fun `replace device with different IDs in the URL and the document should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${computerId}",
                    "type": "display",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}"
                }
            """
            // 'sizeInInch' is not a valid field
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify(exactly = 0) { deviceService.replaceDevice(any()) }
    }

    @Test
    fun `replace device with the wrong type specified in the document should lead to response 409 conflict`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Display::class

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            // 'sizeInInch' is not a valid field
            content = """
                {
                    "id": "${computerId}",
                    "type": "computer",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isConflict() }
            content { empty() }
        }

        verify(exactly = 0) { deviceService.replaceDevice(any()) }
    }

    @Test
    fun `replace device with unknown fields should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${displayId}",
                    "type": "display",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}",
                    "sizeInInch": 19 
                }
            """
            // 'sizeInInch' is not a valid field
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify(exactly = 0) { deviceService.replaceDevice(any()) }
    }

    @Test
    fun `replace display with computer data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.replace(device) is relaxed

        // when / then
        mockMvc.put("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${displayId}",
                    "type": "display",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `replace unknown device should lead to response 404 not found`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } throws DeviceNotFoundException(computerId)

        // when / then
        mockMvc.put("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "id": "${computer.id}",
                    "type": "computer",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isNotFound() }
            content {
                json(
                    """
                        {
                            "messages": [
                                "A device with id macpro-m1-95014 was not found."
                            ]
                        }
                    """
                )
            }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }
}
