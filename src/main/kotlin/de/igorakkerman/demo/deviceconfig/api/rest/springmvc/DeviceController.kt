package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.NoSuchDeviceException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceService: DeviceService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{deviceId}")
    fun findDeviceById(@PathVariable deviceId: DeviceId): DeviceDocument {
        return deviceService.findDeviceById(deviceId)
            ?.toDocument()
            ?.also { log.info { "Device found. document: $it" } }
            ?: throw ResponseStatusException(NOT_FOUND, "Device not found. deviceId=$deviceId")
                .also {log.info {it}}
    }

    @GetMapping
    fun findAllDevices(): List<DeviceDocument> {
        return deviceService.findAllDevices().map { it.toDocument() }
            .also { log.info { "Devices found. document: $it" } }
    }

    @PostMapping
    @ResponseStatus(CREATED)
    fun createDevice(@RequestBody deviceDocument: DeviceDocument) {
        deviceService.createDevice(deviceDocument.toDevice()
            .also { log.info { "Creating device. document: $it" } }
        )
        // TODO: return ID of/URL to resource in header/body
    }

    @PatchMapping("/{deviceId}")
    fun updateDevice(@PathVariable deviceId: DeviceId, @RequestBody requestBody: String) {
        val mapper = jacksonObjectMapper()
        val device = deviceService.findDeviceById(deviceId) ?: throw NoSuchDeviceException(deviceId)

        val deviceUpdateDocument = when (device) {
            is Computer -> mapper.readValue<ComputerUpdateDocument>(requestBody)
            is Display -> mapper.readValue<DisplayUpdateDocument>(requestBody)
        }

        return deviceService.updateDevice(deviceId, deviceUpdateDocument.toUpdate())
    }

    @ExceptionHandler(DeviceAreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun deviceAlreadyExists(exception: DeviceAreadyExistsException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} already exists.")
}
