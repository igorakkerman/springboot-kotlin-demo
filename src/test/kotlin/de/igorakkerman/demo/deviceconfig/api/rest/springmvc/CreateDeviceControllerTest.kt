package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.ninjasquad.springmockk.MockkBean
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class, LogbookAutoConfiguration::class])
class CreateDeviceControllerTest(
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
    fun `create computer with valid data should lead to response 201 created`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
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

        verify {
            deviceService.createDevice(computer)
        }
    }

    @Test
    fun `create display with valid data should lead to response 201 created`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "type": "display",
                    "id": "${display.id}",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}"
                }
            """
        }.andExpect {
            status { isCreated() }
            content { empty() }
        }

        verify {
            deviceService.createDevice(display)
        }
    }

    @Test
    fun `create device with incomplete data should lead to response 400 bad request`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            // mandatory password value is missing
            content = """
                {
                    "type": "computer",
                    "id": "${computer.id}",
                    "name": "${computer.name}",
                    "username": "${computer.username}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify {
            deviceService wasNot Called
        }
    }

    @Test
    fun `create device with forbidden null id should lead to response 400 bad request`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            // mandatory password value is missing
            content = """
                {
                    "type": "computer",
                    "id": null,
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

        verify {
            deviceService wasNot Called
        }
    }

    @Test
    fun `create device with forbidden null name should lead to response 400 bad request`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            // mandatory name value is missing
            content = """
                {
                    "type": "computer",
                    "id": "${computer.id}",
                    "name": null,
                    "username": "${computer.username}",
                    "password": "${computer.password}",
                    "ipAddress": "${computer.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify {
            deviceService wasNot Called
        }
    }

    @Test
    fun `create device with invalid values should lead to response 400 bad request`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            // ipAddress must have IPv4 format
            content = """
                {
                    "type": "computer",
                    "id": "${computer.id}",
                    "name": "${computer.name}",
                    "username": "fartoolongusername",
                    "password": "abc",
                    "ipAddress": "::1"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            content {
                json(
                    """
                        {
                            "messages": [
                                "Invalid value. field: username, message: length must be between 4 and 12",
                                "Invalid value. field: password, message: length must be between 8 and 32",
                                "Invalid value. field: ipAddress, message: IPv4 address has invalid format"
                            ]
                        }
                    """
                )
            }
        }

        verify {
            deviceService wasNot Called
        }
    }

    @Test
    fun `create device with unknown fields should lead to response 400 bad request`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when / then
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            // 'sizeInInch' is not a valid field
            content = """
                {
                    "id": "${displayId}",
                    "type": "display",
                    "name": "${display.name}",
                    "resolution": "${display.resolution}",
                    "sizeInInch": 19 
                }
            """
        }.andExpect {
            status { isBadRequest() }
            content { empty() }
        }

        verify { deviceService wasNot Called }
    }


    @Test
    fun `create computer with existing id should lead to response 409 conflict`() {
        // given
        every { deviceService.createDevice(computer) } throws DeviceAreadyExistsException(computer.id)

        // when / then
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
            status { isConflict() }
            content {
                json(
                    """
                        {
                            "messages": [
                                "A device with id ${computer.id} already exists."
                            ]
                        }
                    """
                )
            }
        }

        verify {
            deviceService.createDevice(computer)
        }
    }
}
