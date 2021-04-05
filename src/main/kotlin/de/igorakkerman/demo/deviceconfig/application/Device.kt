package de.igorakkerman.demo.deviceconfig.application

import kotlin.reflect.KClass

typealias DeviceId = String
typealias DeviceType = KClass<out Device>

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

data class Display(
    override val id: DeviceId,
    override val name: String,
    val resolution: Resolution
) : Device()

enum class Resolution { HD, WQHD, UHD }
