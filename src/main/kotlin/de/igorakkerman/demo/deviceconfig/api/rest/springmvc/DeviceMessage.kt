package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    Type(value = ComputerMessage::class, name = "computer"),
    Type(value = DisplayMessage::class, name = "display"),
)
sealed class DeviceMessage(
    open val id: DeviceId,
    open val name: String,
) {
    abstract fun toDevice(): Device
}

data class ComputerMessage(
    override val id: DeviceId,
    override val name: String,
    val username: String,
    val password: String,
    val ipAddress: String,
) : DeviceMessage(id, name) {
    override fun toDevice() = Computer(
        id = id,
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress
    )
}

data class DisplayMessage(
    override val id: DeviceId,
    override val name: String,
    val resolution: Resolution,
) : DeviceMessage(id, name) {
    override fun toDevice() = Display(
        id = id,
        name = name,
        resolution = resolution,
    )
}

fun Device.toMessage(): DeviceMessage =
    when (this) {
        is Computer -> this.toMessage()
        is Display -> this.toMessage()
    }

fun Computer.toMessage() = ComputerMessage(
    id = this.id,
    name = this.name,
    username = this.username,
    password = this.password,
    ipAddress = this.ipAddress,
)

fun Display.toMessage(): DisplayMessage = DisplayMessage(
    id = this.id,
    name = this.name,
    resolution = this.resolution
)

data class ErrorResponseBody(val message: String)
