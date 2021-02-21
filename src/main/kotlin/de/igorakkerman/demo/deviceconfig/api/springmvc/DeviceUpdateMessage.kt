package de.igorakkerman.demo.deviceconfig.api.springmvc

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ComputerUpdateMessage::class, name = "computer"),
    JsonSubTypes.Type(value = DisplayUpdateMessage::class, name = "display"),
)
sealed class DeviceUpdateMessage(
    open var name: String?,
) {
    abstract fun toUpdate(): DeviceUpdate
}

data class ComputerUpdateMessage(
    override var name: String? = null,
    var username: String? = null,
    var password: String? = null,
    var ipAddress: String? = null,
) : DeviceUpdateMessage(name) {
    override fun toUpdate() = ComputerUpdate(
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress
    )
}

data class DisplayUpdateMessage(
    override var name: String? = null,
    val resolution: Resolution? = null,
) : DeviceUpdateMessage(name) {
    override fun toUpdate() = DisplayUpdate(
        name = name,
        resolution = resolution,
    )
}
