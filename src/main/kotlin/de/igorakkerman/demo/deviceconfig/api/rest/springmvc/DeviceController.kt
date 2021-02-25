package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
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
import kotlin.reflect.KClass

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceService: DeviceService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{deviceId}")
    fun findDeviceById(@PathVariable deviceId: DeviceId): DeviceDocument {
        return deviceService
            .findDeviceById(deviceId)
            .toDocument()
            .also { log.info { "Device found. document: $it" } }
    }

    @GetMapping
    fun findAllDevices(): List<DeviceDocument> {
        return deviceService
            .findAllDevices()
            .map { it.toDocument() }
            .also { log.info { "Devices found. document: $it" } }
    }

    @PostMapping
    @ResponseStatus(CREATED)
    fun createDevice(@RequestBody deviceDocument: DeviceDocument) {
        log.info("Creating device. document: $deviceDocument")

        deviceService.createDevice(deviceDocument.toDevice())

        // TODO: return ID of/URL to resource in header/body
    }

    @PatchMapping("/{deviceId}")
    @Suppress("MoveVariableDeclarationIntoWhen")
    fun updateDevice(@PathVariable deviceId: DeviceId, @RequestBody updateDocument: String) {
        try {
            log.info("Updating device. deviceId: $deviceId, document: $updateDocument")

            val mapper = jacksonObjectMapper()
            val deviceType: KClass<out Device> = deviceService.findDeviceTypeById(deviceId)
            log.debug("Device exists. deviceId: $deviceId, deviceType: ${deviceType.simpleName}")

            val deviceUpdateDocument = when (deviceType) {
                Computer::class -> mapper.readValue<ComputerUpdateDocument>(updateDocument)
                Display::class -> mapper.readValue<DisplayUpdateDocument>(updateDocument)
                else -> throw IllegalStateException("Unexpected bad type!")
            }

            // TODO: return ID of/URL to resource in header/body
            deviceService.updateDevice(deviceId, deviceUpdateDocument.toUpdate())

            log.info("Device updated. deviceId: $deviceId")
        } catch (exception: JsonProcessingException) {
            throw (ResponseStatusException(BAD_REQUEST, "Error processing update request document. ${exception.originalMessage}", exception)
                .also { log.info(it) { "" } })
        }
    }

    @ExceptionHandler(DeviceNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun deviceAlreadyExists(exception: DeviceNotFoundException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} was not found.")

    @ExceptionHandler(DeviceAreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun deviceAlreadyExists(exception: DeviceAreadyExistsException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} already exists.")
}
