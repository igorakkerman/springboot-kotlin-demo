package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.api.rest.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [Application::class, ServiceConfiguration::class])
class DeviceControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"
    private val computer = Computer(computerId, "best mac", "timapple", "0n3m0r3th1ng", "192.168.178.1")
    private val displayId = "samsung-screen-88276"
    private val display = Display(displayId, "favorite screen", Resolution.UHD)

    @Test
    fun `GET computer found by id should lead to response 200 with device data`() {
        // given

        every { deviceService.findDeviceById(computerId) } returns computer

        // when
        mockMvc.get("/devices/$computerId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { APPLICATION_JSON }
            content {
                json(
                    """
                            {
                                "type": "computer",
                                "id": "${computer.id}",
                                "name": "${computer.name}",
                                "username": "${computer.username}",
                                "password": "${computer.password}",
                                "ipAddress": "${computer.ipAddress}"
                            }
                    """,
                    strict = true
                )
            }
        }
    }

    @Test
    fun `GET display found by id should lead to response 200 with device data`() {

        // given

        every { deviceService.findDeviceById(displayId) } returns display

        // when
        mockMvc.get("/devices/$displayId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { APPLICATION_JSON }
            content {
                json(
                    """
                        {
                            "type": "display",
                            "id": "${display.id}",
                            "name": "${display.name}",
                            "resolution": "${display.resolution.name}"
                        }
                    """,
                    strict = true
                )
            }
        }
    }

    @Test
    fun `GET device not found by id should lead to response 404 not found`() {
        val deviceId = "amiga2000-007"
        every { deviceService.findDeviceById(deviceId) } returns null

        mockMvc.get("/devices/$deviceId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `find device by id request with wrong method should lead to reponse 405 method not allowed`() {
        mockMvc.post("/devices/$computerId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isMethodNotAllowed() }
        }
    }

    @Test
    fun `POST create computer with valid data should lead to response 201 created`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "type": "computer",
                    "id": "${computer.id}",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    @Disabled
    fun `POST create computer with missing body should lead to response 409 conflict`() {
        // given

        // when
        mockMvc.post("/devices")
            .andExpect {
                status { isBadRequest() }
            }
    }
}
