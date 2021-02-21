package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.api.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class, ServiceConfiguration::class])
class ApplicationConfigTest(
        @Autowired
        private val deviceController: DeviceController
) {
    @Test
    fun contextLoads() {
        deviceController shouldNotBe null
    }
}
