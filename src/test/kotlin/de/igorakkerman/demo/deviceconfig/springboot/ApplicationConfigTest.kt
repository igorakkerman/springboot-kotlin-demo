package de.igorakkerman.demo.deviceconfig.springboot

import de.igorakkerman.demo.deviceconfig.api.rest.springmvc.DeviceController
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.presistence.springdatajpa.JpaDeviceRepository
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan

@SpringBootTest(classes = [Application::class])
class ApplicationConfigTest(
    @Autowired
    private val deviceController: DeviceController
) {
    @Test
    fun contextLoads() {
        deviceController shouldNotBe null
    }
}