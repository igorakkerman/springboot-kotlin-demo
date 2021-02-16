package de.igorakkerman.demo.deviceconfig.start

import DataStore
import DeviceService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication(scanBasePackages = ["de.igorakkerman.demo.deviceconfig"])
class DeviceConfigApplication {
    @Bean
    fun deviceService(dataStore: DataStore) = DeviceService(dataStore)
}

fun main(commandLineArguments: Array<String>) {
    runApplication<DeviceConfigApplication>(*commandLineArguments)
}
