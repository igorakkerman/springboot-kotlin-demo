package de.igorakkerman.demo.deviceconfig.persistence

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType.TABLE_PER_CLASS
import javax.validation.constraints.Pattern

@Entity
@Inheritance(strategy = TABLE_PER_CLASS)
sealed class DeviceEntity(
        @Id
        open val id: DeviceId,

        @Column(name = "name")
        open val name: String,
) {
    abstract fun toDevice(): Device
}

@Entity
data class ComputerEntity(
        override val id: DeviceId,

        override val name: String,

        @Column
        private val username: String,

        @Column
        private val password: String,

        @Column
        @Pattern(regexp = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$")
        private val ipAddress: String,
) : DeviceEntity(id, name) {
    override fun toDevice() = Computer(
            id = id,
            name = name,
            username = username,
            password = password,
            ipAddress = ipAddress
    )
}

fun Device.toEntity(): DeviceEntity =
        when (this) {
            is Computer -> this.toEntity()
        }

fun Computer.toEntity(): ComputerEntity = ComputerEntity(
        id = this.id,
        name = this.name,
        username = this.username,
        password = this.password,
        ipAddress = this.ipAddress,
)
