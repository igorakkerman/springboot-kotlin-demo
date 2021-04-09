package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.DeviceAreadyExistsException
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.DeviceTypeConflictException
import de.igorakkerman.demo.deviceconfig.application.Display
import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

const val APPLICATION_MERGE_PATCH_JSON_VALUE = "application/merge-patch+json"

@RestController
@RequestMapping("/devices")
class DeviceController(
    private val deviceService: DeviceService
) {
    private val log = KotlinLogging.logger {}

    @GetMapping("/{deviceId}", produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findDeviceById(@PathVariable deviceId: DeviceId): DeviceDocument {
        return deviceService
            .findDeviceById(deviceId)
            .toDocument()
            .also { log.info { "Device found. document: $it" } }
    }

    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findAllDevices(): List<DeviceDocument> {
        return deviceService
            .findAllDevices()
            .map { it.toDocument() }
            .also { log.info { "Devices found. document: $it" } }
    }

    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    fun createDevice(@Valid @RequestBody deviceDocument: DeviceDocument) {
        log.info("Creating device. document: $deviceDocument")

        deviceService.createDevice(deviceDocument.toDevice())

        log.info("Device created. deviceId: ${deviceDocument.id}")
    }

    @PutMapping("/{deviceId}", consumes = [APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    fun replaceDevice(@PathVariable deviceId: DeviceId, @RequestBody deviceDocument: DeviceDocument) {
        log.info("Replacing device. deviceId: $deviceId, document: $deviceDocument")

        if (deviceId != deviceDocument.id)
            throw DeviceIdConflictException(resourceId = deviceId, documentDeviceId = deviceDocument.id)

        deviceService.replaceDevice(deviceDocument.toDevice())

        log.info("Device replaced. deviceId: $deviceId")
    }

    @PatchMapping("/{deviceId}", consumes = [APPLICATION_MERGE_PATCH_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    fun updateDevice(@PathVariable deviceId: DeviceId, @RequestBody updateDocumentContent: String) {
        log.info("Updating device. deviceId: $deviceId, JSON document: $updateDocumentContent")

        deviceService.updateDevice(deviceId) { deviceType ->
            val mapper = jacksonObjectMapper()
            val updateDocument = when (deviceType) {
                Computer::class -> mapper.readValue<ComputerUpdateDocument>(updateDocumentContent)
                Display::class -> mapper.readValue<DisplayUpdateDocument>(updateDocumentContent)
                else -> throw IllegalStateException("Unexpected bad type!")
            }
            updateDocument.toUpdate()
                .also { log.info("deviceUpdate: $it") }
        }
        log.info("Device updated. deviceId: $deviceId")
    }

    @ExceptionHandler(DeviceNotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    fun deviceAlreadyExists(exception: DeviceNotFoundException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} was not found.")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(BAD_REQUEST)
    fun validationError(exception: MethodArgumentNotValidException): ErrorResponseBody =
        ErrorResponseBody(
            exception
                .allErrors
                .map { "Invalid value. field: ${(it as FieldError).field}, message: ${it.defaultMessage}" }
                .toTypedArray()
        )

    @ExceptionHandler(DeviceAreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun deviceAlreadyExists(exception: DeviceAreadyExistsException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} already exists.")

    @ExceptionHandler(DeviceTypeConflictException::class)
    @ResponseStatus(CONFLICT)
    fun deviceTypeConflict(exception: DeviceTypeConflictException): ErrorResponseBody =
        ErrorResponseBody(
            "Type of resource with id ${exception.deviceId} doesn't match device type in document. " +
                    "resourceType: ${exception.existingDeviceType.resourceType()}, invalidDeviceType: ${exception.invalidDeviceType.resourceType()}"
        )

    @ExceptionHandler(DeviceIdConflictException::class)
    @ResponseStatus(BAD_REQUEST)
    internal fun deviceIdConflict(exception: DeviceIdConflictException): ErrorResponseBody =
        ErrorResponseBody(
            "Resource ID in URL doesn't match device ID in document. resourceId: ${exception.resourceId}, deviceId: ${exception.documentDeviceId}"
        )

    @ExceptionHandler(JsonProcessingException::class)
    @ResponseStatus(BAD_REQUEST)
    internal fun deviceIdConflict(exception: JsonProcessingException): ErrorResponseBody =
        ErrorResponseBody(
            "Error processing update request document. ${
                exception.originalMessage
                    // clean up message from internal details, such as class names
                    // won't break if Jackson changes their strings    
                    .replace(Regex("Instantiation of \\[.+] value f"), "F")
                    .replace(Regex(" \\(class.+\\)"), "")
            }"
        )

    internal class DeviceIdConflictException(val resourceId: DeviceId, val documentDeviceId: DeviceId) : RuntimeException()
}
