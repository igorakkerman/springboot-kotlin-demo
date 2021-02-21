package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.api.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [Application::class])
class DeviceControllerTest {

    @MockkBean
    private lateinit var deviceService: DeviceService

    @Autowired
    private lateinit var deviceController: DeviceController

    @Test
    fun someTest() {
        val deviceId = "macpro-m1-95014"
        val computer = Computer(deviceId, "bestmac", "timapple", "1tsm3", "192.168.178.1")

        every { deviceService.findDeviceById(deviceId) } returns computer

        deviceController.findDeviceById(deviceId) shouldBe computer
    }
}
