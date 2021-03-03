package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import de.igorakkerman.demo.deviceconfig.validation.Ipv4Address
import de.igorakkerman.demo.deviceconfig.validation.Password
import de.igorakkerman.demo.deviceconfig.validation.Username

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    Type(value = ComputerDocument::class, name = "computer"),
    Type(value = DisplayDocument::class, name = "display"),
)
sealed class DeviceDocument(
    open val id: DeviceId,
    open val name: String,
) {
    abstract fun toDevice(): Device
}

data class ComputerDocument(
    override val id: DeviceId,
    override val name: String,
    @Username
    val username: String,
    @Password
    val password: String,
    @Ipv4Address
    val ipAddress: String,
) : DeviceDocument(id, name) {
    override fun toDevice() = Computer(
        id = id,
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress
    )
}

data class DisplayDocument(
    override val id: DeviceId,
    override val name: String,
    val resolution: Resolution,
) : DeviceDocument(id, name) {
    override fun toDevice() = Display(
        id = id,
        name = name,
        resolution = resolution,
    )
}

fun Device.toDocument(): DeviceDocument =
    when (this) {
        is Computer -> this.toDocument()
        is Display -> this.toDocument()
    }

fun Computer.toDocument() = ComputerDocument(
    id = this.id,
    name = this.name,
    username = this.username,
    password = this.password,
    ipAddress = this.ipAddress,
)

fun Display.toDocument(): DisplayDocument = DisplayDocument(
    id = this.id,
    name = this.name,
    resolution = this.resolution
)

@Suppress("ArrayInDataClass")
data class ErrorResponseBody(val messages: Array<String>) {
    constructor(message: String) : this(arrayOf(message))
}
