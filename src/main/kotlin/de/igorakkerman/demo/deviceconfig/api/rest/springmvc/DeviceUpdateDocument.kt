package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution

sealed class DeviceUpdateDocument(
    open val name: String,
) {
    abstract fun toUpdate(): DeviceUpdate
}

data class ComputerUpdateDocument(
    override val name: String = UNSET,
    val username: String = UNSET,
    val password: String = UNSET,
    val ipAddress: String = UNSET,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = ComputerUpdate(
        name = of(name),
        username = of(username),
        password = of(password),
        ipAddress = of(ipAddress),
    )
}

data class DisplayUpdateDocument(
    override val name: String = UNSET,
    val resolution: DocumentResolution = DocumentResolution.UNSET,
) : DeviceUpdateDocument(name) {
    override fun toUpdate() = DisplayUpdate(
        name = of(name),
        resolution = of(resolution),
    )
}

// marker for unset values, to be differentiated from null
// in JSON Merge Patch, null has the meaning of deletion, which is not allowed here
// application/merge-patch+json (https://tools.ietf.org/html/rfc7396)
internal const val UNSET = "_UNSET_"

@Suppress("unused") // required by Jackson's JSON deserialization
enum class DocumentResolution { FHD, WQHD, UHD, UNSET }

private fun of(value: String) = if (value !== UNSET) value else null
private fun of(value: DocumentResolution) = if (value !== DocumentResolution.UNSET) Resolution.valueOf(value.name) else null
