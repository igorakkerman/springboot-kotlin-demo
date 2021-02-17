package de.igorakkerman.demo.deviceconfig

import de.igorakkerman.demo.deviceconfig.start.DeviceConfigApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DeviceConfigApplication::class])
class DeviceConfigApplicationTest {

    @Test
    fun contextLoads() {
    }
}
