package de.igorakkerman.demo.deviceconfig.persistence

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.Resolution
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType.TABLE_PER_CLASS
import javax.persistence.Table
import javax.validation.constraints.Pattern

@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
sealed class DeviceEntity(
        @Id
        open val id: DeviceId,

        @Column(nullable = false)
        open val name: String,
) {
    abstract fun toDevice(): Device
}

@Entity
@Table(name = "computer")
data class ComputerEntity(
        override val id: DeviceId,

        override val name: String,

        @Column(nullable = false)
        val username: String,

        @Column(nullable = false)
        val password: String,

        @Column(nullable = false)
        @field:Pattern(
                regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$",
                message = "field: IPv4 address has invalid format",
        )
        val ipAddress: String,
) : DeviceEntity(id, name) {
    override fun toDevice() = Computer(
            id = id,
            name = name,
            username = username,
            password = password,
            ipAddress = ipAddress
    )
}

@Entity
@Table(name = "display")
data class DisplayEntity(
        override val id: DeviceId,

        override val name: String,

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        val resolution: Resolution,
) : DeviceEntity(id, name) {
    override fun toDevice() = Display(
            id = id,
            name = name,
            resolution = resolution,
    )
}

fun Device.toEntity(): DeviceEntity =
        when (this) {
            is Computer -> this.toEntity()
            is Display -> this.toEntity()
        }

fun Computer.toEntity(): ComputerEntity = ComputerEntity(
        id = this.id,
        name = this.name,
        username = this.username,
        password = this.password,
        ipAddress = this.ipAddress,
)

fun Display.toEntity(): DisplayEntity = DisplayEntity(
        id = this.id,
        name = this.name,
        resolution = this.resolution
)
