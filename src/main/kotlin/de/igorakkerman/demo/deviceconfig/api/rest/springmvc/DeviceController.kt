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
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass

const val ACCEPT_PATCH_HEADER = "Accept-Patch"
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
    fun createDevice(@RequestBody deviceDocument: DeviceDocument) {
        log.info("Creating device. document: $deviceDocument")

        deviceService.createDevice(deviceDocument.toDevice())

        // TODO: return ID of/URL to resource in header/body
    }

    @PutMapping("/{deviceId}", consumes = [APPLICATION_JSON_VALUE])
    fun replaceDevice(@PathVariable deviceId: DeviceId, @RequestBody deviceDocument: DeviceDocument) {
        try {
            log.info("Replacing device. deviceId: $deviceId, document: $deviceDocument")

            if (deviceId != deviceDocument.id)
                throw ResponseStatusException(BAD_REQUEST, "Resource ID in URL doesn't match device ID in document. resourceId: $deviceId, deviceId: ${deviceDocument.id}")
                    .also { log.info { it.message } }

            val device = deviceDocument.toDevice()

            val deviceType: KClass<out Device> = deviceService.findDeviceTypeById(deviceId)
            log.debug("Device exists. deviceId: $deviceId, deviceType: ${deviceType.simpleName}")

            if (device::class != deviceType)
                throw ResponseStatusException(CONFLICT, "Type of resource with specified ID doesn't match device type in document. resourceType: $deviceType, deviceType: ${device::class}")
                    .also { log.info(it.message) }

            // TODO: return ID of/URL to resource in header/body
            deviceService.replaceDevice(deviceDocument.toDevice())

            log.info("Device replaced. deviceId: $deviceId")
        } catch (exception: JsonProcessingException) {
            throw (ResponseStatusException(BAD_REQUEST, "Error processing replace request document. ${exception.originalMessage}", exception))
                .also { log.info { it.message } }
        }
    }

    @PatchMapping("/{deviceId}", consumes = [APPLICATION_MERGE_PATCH_JSON_VALUE])
    fun mergeIntoDevice(@PathVariable deviceId: DeviceId, @RequestBody updateDocument: String, response: HttpServletResponse) {
        try {
            log.info("Merging into device. deviceId: $deviceId, document: $updateDocument")

            response.addHeader(ACCEPT_PATCH_HEADER, APPLICATION_MERGE_PATCH_JSON_VALUE)

            val mapper = jacksonObjectMapper()
            val deviceType: KClass<out Device> = deviceService.findDeviceTypeById(deviceId)
            log.debug("Device exists. deviceId: $deviceId, deviceType: ${deviceType.simpleName}")

            val deviceUpdateDocument = when (deviceType) {
                Computer::class -> mapper.readValue<ComputerUpdateDocument>(updateDocument)
                Display::class -> mapper.readValue<DisplayUpdateDocument>(updateDocument)
                else -> throw IllegalStateException("Unexpected bad type!")
            }

            // TODO: return ID of/URL to resource in header/body
            deviceService.mergeIntoDevice(deviceId, deviceUpdateDocument.toUpdate())

            log.info("Merged into device. deviceId: $deviceId")
        } catch (exception: JsonProcessingException) {
            throw (ResponseStatusException(BAD_REQUEST, "Error processing update request document. ${exception.originalMessage}", exception)
                .also { log.info { it.message } })
        }
    }

    // if used with a wrong media type, provide the Accept-Patch header as additional information
    @PatchMapping("/{deviceId}")
    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    fun mergeIntoDeviceWrongMediaType(@PathVariable deviceId: DeviceId, response: HttpServletResponse) {
        response.addHeader(ACCEPT_PATCH_HEADER, APPLICATION_MERGE_PATCH_JSON_VALUE)
    }

    @RequestMapping("/{deviceId}", method = [RequestMethod.OPTIONS])
    @ResponseStatus(NO_CONTENT)
    fun options(response: HttpServletResponse) {
        response.addHeader(ACCEPT_PATCH_HEADER, APPLICATION_MERGE_PATCH_JSON_VALUE)
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
