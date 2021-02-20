package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.boot.Application
import de.igorakkerman.demo.deviceconfig.boot.ServiceConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class, ServiceConfiguration::class])
class ApplicationConfigTest {

    @Test
    fun contextLoads() {
    }
}
