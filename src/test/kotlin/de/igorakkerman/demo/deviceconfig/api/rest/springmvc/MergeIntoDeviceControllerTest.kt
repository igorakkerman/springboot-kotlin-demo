package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.result.HeaderResultMatchersDsl

private val APPLICATION_MERGE_PATCH_JSON =
    MediaType.parseMediaType(APPLICATION_MERGE_PATCH_JSON_VALUE)

internal fun HeaderResultMatchersDsl.acceptMergePatch() =
    string(ACCEPT_PATCH_HEADER, APPLICATION_MERGE_PATCH_JSON_VALUE)

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class MergeIntoDeviceControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"
    private val computerUpdate = ComputerUpdate(name = "best mac", username = "timapple", password = "0n3m0r3th1ng", ipAddress = "192.168.178.1")
    private val computerUpdatePartial = ComputerUpdate(name = "second best mac", ipAddress = "127.0.0.1")
    private val displayId = "samsung-screen-88276"
    private val displayUpdate = DisplayUpdate("second best screen", resolution = WQHD)

    @Test
    fun `merging full valid data into computer should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.merge(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${computerUpdate.name}",
                    "username": "${computerUpdate.username}",
                    "password": "${computerUpdate.password}",
                    "ipAddress": "${computerUpdate.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
            header { acceptMergePatch() }
        }

        verify { deviceService.mergeIntoDevice(computerId, computerUpdate) }
    }

    @Test
    fun `merging full valid data into display should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.merge(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${displayUpdate.name}",
                    "resolution": "${displayUpdate.resolution}"
                }
            """
        }.andExpect {
            status { isOk() }
            header { acceptMergePatch() }
        }

        verify { deviceService.mergeIntoDevice(displayId, displayUpdate) }
    }

    @Test
    fun `merging partial valid data into computer should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.merge(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${computerUpdatePartial.name}",
                    "ipAddress": "${computerUpdatePartial.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
            header { acceptMergePatch() }
        }

        verify { deviceService.mergeIntoDevice(computerId, computerUpdatePartial) }
    }

    @Test
    fun `merging unknown fields into device should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            // 'id' is not a valid updatable field, it is part of the URL
            content = """
                {
                    "id": "${displayId}",
                    "name": "${displayUpdate.name}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header { acceptMergePatch() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.mergeIntoDevice(any(), any()) }
    }

    @Test
    fun `merging forbidden null id into device should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            // 'id' is not a valid updatable field, it is part of the URL
            content = """
                {
                    "id": null,
                    "name": "${displayUpdate.name}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header { acceptMergePatch() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.mergeIntoDevice(any(), any()) }
    }

    @Test
    fun `merging forbidden null value into device should lead to response 400 bad request`() {
        // in JSON Merge Patch, null has the meaning of deletion, which is not allowed here
        // application/merge-patch+json (https://tools.ietf.org/html/rfc7396)

        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            // 'id' is not a valid updatable field, it is part of the URL
            content = """
                {
                    "name": null
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header { acceptMergePatch() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.mergeIntoDevice(any(), any()) }
    }

    @Test
    // just to be safe that the marker is compared for identity, not equality
    fun `merging our 'unset' marker value should use the literal string and lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        val displayUpdate = DisplayUpdate(name = UNSET)

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            // 'id' is not a valid updatable field, it is part of the URL
            content = """
                {
                    "name": "$UNSET"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify { deviceService.mergeIntoDevice(displayId, displayUpdate) }
    }

    @Test
    fun `merging computer data into display should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.merge(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${computerUpdate.name}",
                    "username": "${computerUpdate.username}",
                    "password": "${computerUpdate.password}",
                    "ipAddress": "${computerUpdate.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
            header { acceptMergePatch() }
        }

        verify(exactly = 0) { deviceService.mergeIntoDevice(any(), any()) }
    }

    @Test
    fun `merging into unknown device should lead to response 404 not found`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } throws DeviceNotFoundException(computerId)

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${computerUpdate.name}",
                }
            """
        }.andExpect {
            status { isNotFound() }
            header { acceptMergePatch() }
        }

        verify(exactly = 0) { deviceService.mergeIntoDevice(any(), any()) }
    }
}
