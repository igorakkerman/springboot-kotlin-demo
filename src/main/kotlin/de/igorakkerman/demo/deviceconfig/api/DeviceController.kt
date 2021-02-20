package de.igorakkerman.demo.deviceconfig.api

import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.NoSuchItemException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import de.igorakkerman.demo.deviceconfig.application.Display
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.websocket.server.PathParam

@RestController
@RequestMapping("/devices")
class DeviceController(
        private val deviceService: DeviceService
) {
    @GetMapping("/{deviceId}")
    fun findDeviceById(deviceId: DeviceId): Device? {
        return deviceService.findDeviceById(deviceId)
    }

    @GetMapping
    fun findAllDevices(): List<DeviceMessage> {
        return deviceService.findAllDevices().map { it.toMessage() }
    }

    @PostMapping
    fun createDevice(DeviceMessage: DeviceMessage) {

        return deviceService.createDevice(DeviceMessage.toDevice())
    }

    @PatchMapping("/{deviceId}")
    fun updateDevice(@PathParam("deviceId") deviceId: String, @RequestBody requestBody: String) {
        val mapper = jacksonObjectMapper()
        val device = deviceService.findDeviceById(deviceId) ?: throw NoSuchItemException(deviceId)

        val deviceUpdateMessage = when(device) {
            is Computer -> mapper.readValue<ComputerUpdateMessage>(requestBody)
            is Display -> mapper.readValue<DisplayUpdateMessage>(requestBody)
        }

        return deviceService.updateDevice(deviceId, deviceUpdateMessage.toUpdate())
    }
}

/*
{
    "devices" = [
        {
            "type": "computer"
            "name": "...",
            "ipAddress": "1.1.1.1",
            ...
        },
        {
            "type": "display",
            "name": "...",
            "resolution: "HD"
        },
        ...
    ]
}
*/
