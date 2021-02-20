package de.igorakkerman.demo.deviceconfig.application

sealed class DeviceUpdate(
        open val name: String?,
)

data class ComputerUpdate(
        override val name: String?,
        val username: String?,
        val password: String?,
        val ipAddress: String?,
) : DeviceUpdate(name)

data class DisplayUpdate(
        override val name: String? = null,
        val resolution: Resolution? = null,
) : DeviceUpdate(name)
