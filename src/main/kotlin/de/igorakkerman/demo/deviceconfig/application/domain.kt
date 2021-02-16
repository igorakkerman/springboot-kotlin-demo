package de.igorakkerman.demo.deviceconfig.application

typealias DeviceId = String

sealed class Device {
    abstract val id: DeviceId
    abstract val name: String
}

data class Computer(
        override val id: DeviceId,
        override val name: String,
        val username: String,
        val password: String,
        val ipAddress: String
) : Device()
