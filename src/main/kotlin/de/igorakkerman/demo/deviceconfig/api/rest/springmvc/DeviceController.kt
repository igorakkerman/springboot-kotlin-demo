package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
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
    @GetMapping("/{deviceId}")
    fun findDeviceById(@PathVariable deviceId: DeviceId): Device? {
        return deviceService.findDeviceById(deviceId)
            ?: throw ResponseStatusException(NOT_FOUND, "Device not found. deviceId=$deviceId")
    }

    @GetMapping
    fun findAllDevices(): List<DeviceMessage> {
        return deviceService.findAllDevices().map { it.toMessage() }
    }

    @PostMapping
    @ResponseStatus(CREATED)
    fun createDevice(@RequestBody deviceMessage: DeviceMessage) {
        deviceService.createDevice(deviceMessage.toDevice())

        // TODO: return ID of/URL to resource in header/body
    }

    @PatchMapping("/{deviceId}")
    fun updateDevice(@PathVariable deviceId: String, @RequestBody requestBody: String) {
        val mapper = jacksonObjectMapper()
        val device = deviceService.findDeviceById(deviceId) ?: throw NoSuchDeviceException(deviceId)

        val deviceUpdateMessage = when (device) {
            is Computer -> mapper.readValue<ComputerUpdateMessage>(requestBody)
            is Display -> mapper.readValue<DisplayUpdateMessage>(requestBody)
        }

        return deviceService.updateDevice(deviceId, deviceUpdateMessage.toUpdate())
    }

    @ExceptionHandler(DeviceAreadyExistsException::class)
    @ResponseStatus(CONFLICT)
    fun deviceAlreadyExists(exception: DeviceAreadyExistsException): ErrorResponseBody =
        ErrorResponseBody("A device with id ${exception.deviceId} already exists.")
}
