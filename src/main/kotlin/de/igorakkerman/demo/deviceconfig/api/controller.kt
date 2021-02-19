package de.igorakkerman.demo.deviceconfig.api

import DeviceService
import de.igorakkerman.demo.deviceconfig.application.Device
import de.igorakkerman.demo.deviceconfig.application.DeviceId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
    fun findAllDevices(): List<Device> {
        return deviceService.findAllDevices()
    }

    @PostMapping
    fun createDevice(device: Device) {
        return deviceService.createDevice(device)
    }

    @PutMapping // FIXME: ("/{deviceId}")
    fun updateDevice(device: Device) {
        return deviceService.updateDevice(device)
    }
}