package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution

sealed class DeviceUpdateDocument(
    open val name: String?,
) {
    abstract fun toUpdate(): DeviceUpdate
}

data class ComputerUpdateDocument(
    override val name: String? = null,
    val username: String? = null,
    val password: String? = null,
    val ipAddress: String? = null,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = ComputerUpdate(
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress,
    )
}

data class DisplayUpdateDocument(
    override val name: String? = null,
    val resolution: Resolution? = null,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = DisplayUpdate(
        name = name,
        resolution = resolution,
    )
}
