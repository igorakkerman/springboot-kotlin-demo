package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.api.rest.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [Application::class, ServiceConfiguration::class])
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
        }
            .andExpect {
                status { isCreated() }
            }
    }

    @Test
    fun `POST create computer with existing id should lead to response 409 conflict`() {
        // given
        every { deviceService.createDevice(computer) }
            .returns(Unit)
            .andThenThrows(DeviceAreadyExistsException(computer.id))

        fun postCreateComputerRequest() =
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
            }

        // when/then first request
        postCreateComputerRequest().andExpect {
            status { isCreated() }
        }

        // when/then second request with same id
        postCreateComputerRequest().andExpect {
            status { isConflict() }
            content {
                json(
                    """
                            {
                                "message": "A device with id ${computer.id} already exists."
                            }
                    """
                )
            }
        }
    }

    @Test
    fun `POST create computer with missing body should lead to response 400 bad request`() {
        // when/then
        mockMvc.post("/devices").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST create computer with non-JSON body should lead to response 415 unsupported media type`() {
        // when/then
        mockMvc.post("/devices") {
            contentType = APPLICATION_XML
            content = """<computer id="ourgoodold386" />"""
        }.andExpect {
            status { isUnsupportedMediaType() }
        }
    }
}
