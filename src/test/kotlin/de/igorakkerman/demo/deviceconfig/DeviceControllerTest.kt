package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.api.rest.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
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
//@AutoConfigureMockMvc
@ContextConfiguration(classes = [Application::class, ServiceConfiguration::class])
class DeviceControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean
    private lateinit var deviceService: DeviceService

    @Test
    fun `device found by id should be returned with 200`() {
        // given
        val deviceId = "macpro-m1-95014"
        val computer = Computer(deviceId, "best mac", "timapple", "0n3m0r3th1ng", "192.168.178.1")

        every { deviceService.findDeviceById(deviceId) } returns computer

        // when
        mockMvc.get("/devices/$deviceId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { APPLICATION_JSON }
            content {
                json(
                    """
                        {
                            "id":"${computer.id}",
                            "name":"${computer.name}",
                            "username":"${computer.username}",
                            "password":"${computer.password}",
                            "ipAddress":"${computer.ipAddress}"
                        }
                        """, strict = true
                )
            }
        }
    }

    @Test
    fun `device not found by id should return null with 404`() {
        val deviceId = "amiga2000-007"
        every { deviceService.findDeviceById(deviceId) } returns null

        mockMvc.get("/devices/$deviceId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
        }
    }
}
