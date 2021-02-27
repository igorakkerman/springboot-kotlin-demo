package de.igorakkerman.demo.deviceconfig.presistence.springdatajpa

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType.STRING
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType.TABLE_PER_CLASS
import javax.persistence.Table
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
sealed class DeviceEntity(
    @Id
    open val id: DeviceId,

    @Column(nullable = false)
    open var name: String,
) {
    abstract fun toDevice(): Device
    abstract fun deviceType(): KClass<out Device>
}

@Entity
@Table(name = "computer")
data class ComputerEntity(
    override val id: DeviceId,

    override var name: String,

    @Column(nullable = false)
    var username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    @field:Pattern(
        regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$",
        message = "field: IPv4 address has invalid format",
    )
    var ipAddress: String,
) : DeviceEntity(id, name) {
    override fun toDevice() = Computer(
        id = id,
        name = name,
        username = username,
        password = password,
        ipAddress = ipAddress
    )

    override fun deviceType() = Computer::class
}

@Entity
@Table(name = "display")
data class DisplayEntity(
    override val id: DeviceId,

    override var name: String,

    @Column(nullable = false)
    @Enumerated(STRING)
    var resolution: Resolution,
) : DeviceEntity(id, name) {
    override fun toDevice() = Display(
        id = id,
        name = name,
        resolution = resolution,
    )

    override fun deviceType() = Display::class
}

fun Device.toEntity(): DeviceEntity =
    when (this) {
        is Computer -> this.toEntity()
        is Display -> this.toEntity()
    }

fun Computer.toEntity() = ComputerEntity(
    id = this.id,
    name = this.name,
    username = this.username,
    password = this.password,
    ipAddress = this.ipAddress,
)

fun Display.toEntity() = DisplayEntity(
    id = this.id,
    name = this.name,
    resolution = this.resolution
)

fun DeviceUpdate.mergeIntoEntity(entity: DeviceEntity) =
    when (this) {
        is ComputerUpdate -> this.mergeIntoEntity(entity as ComputerEntity)
        is DisplayUpdate -> this.mergeIntoEntity(entity as DisplayEntity)
    }

fun ComputerUpdate.mergeIntoEntity(entity: ComputerEntity) {
    if (name != null) entity.name = name
    if (username != null) entity.username = username
    if (password != null) entity.password = password
    if (ipAddress != null) entity.ipAddress = ipAddress
}

fun DisplayUpdate.mergeIntoEntity(entity: DisplayEntity) {
    if (name != null) entity.name = name
    if (resolution != null) entity.resolution = resolution
}
