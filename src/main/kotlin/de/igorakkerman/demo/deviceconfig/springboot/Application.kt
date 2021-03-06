package de.igorakkerman.demo.deviceconfig.springboot

import de.igorakkerman.demo.deviceconfig.application.DeviceRepository
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication(scanBasePackages = ["de.igorakkerman.demo.deviceconfig"])
class Application

@Configuration
class ServiceConfiguration {
    @Bean
    fun deviceService(deviceRepository: DeviceRepository) = DeviceService(deviceRepository)
}

fun main(commandLineArguments: Array<String>) {
    runApplication<Application>(*commandLineArguments)
}
