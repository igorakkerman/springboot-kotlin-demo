package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class FindAllDevicesControllerTest(
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
    fun `computer and display found should lead to response 200 OK with device data`() {
        // given

        every { deviceService.findAllDevices() } returns listOf(computer, display)

        // when
        mockMvc.get("/devices") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { APPLICATION_JSON }
            content {
                json(
                    """
                        [
                            {
                                "type": "computer",
                                "id": "${computer.id}",
                                "name": "${computer.name}",
                                "username": "${computer.username}",
                                "password": "${computer.password}",
                                "ipAddress": "${computer.ipAddress}"
                            },
                            {
                                "type": "display",
                                "id": "${display.id}",
                                "name": "${display.name}",
                                "resolution": "${display.resolution.name}"
                            }
                        ]
                    """,
                    strict = true
                )
            }
        }
    }

    @Test
    fun `empty device list should lead to response 200 OK with empty list`() {

        every { deviceService.findAllDevices() } returns emptyList()

        // when
        mockMvc.get("/devices") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { APPLICATION_JSON }
            content { json("[]", strict = true) }
        }
    }
}
