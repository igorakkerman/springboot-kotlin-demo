package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution

sealed class DeviceUpdateDocument(
    open var name: String?,
) {
    abstract fun toUpdate(): DeviceUpdate
}

data class ComputerUpdateDocument(
    override var name: String? = null,
    var username: String? = null,
    var password: String? = null,
    var ipAddress: String? = null,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = ComputerUpdate(
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress
    )
}

data class DisplayUpdateDocument(
    override var name: String? = null,
    val resolution: Resolution? = null,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = DisplayUpdate(
        name = name,
        resolution = resolution,
    )
}
