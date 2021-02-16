package de.igorakkerman.demo.deviceconfig.start

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeviceConfigApplication

fun main(commandLineArguments: Array<String>) {
    runApplication<DeviceConfigApplication>(*commandLineArguments)
}
