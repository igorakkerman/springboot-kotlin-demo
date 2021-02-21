package de.igorakkerman.demo.deviceconfig.application

sealed class DeviceUpdate(
    open val name: String?,
)

data class ComputerUpdate(
    override val name: String? = null,
    val username: String? = null,
    val password: String? = null,
    val ipAddress: String? = null,
) : DeviceUpdate(name)

data class DisplayUpdate(
    override val name: String? = null,
    val resolution: Resolution? = null,
) : DeviceUpdate(name)
