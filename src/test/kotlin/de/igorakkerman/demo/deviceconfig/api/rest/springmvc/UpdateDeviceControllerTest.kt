package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.ninjasquad.springmockk.MockkBean
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class UpdateDeviceControllerTest(
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
    fun `updating full valid data of computer should lead to response 204 no content`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

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
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.updateDevice(computerId, computerUpdate) }
    }

    @Test
    fun `updating full valid data of display should lead to response 204 no content`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

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
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, displayUpdate) }
    }

    @Test
    fun `updating partial valid data of computer should lead to response 204 no content`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

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
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.updateDevice(computerId, computerUpdatePartial) }
    }

    @Test
    fun `updating unknown fields of device should lead to response 400 bad request`() {
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
            content { empty() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `updating id of device by forbidden null should lead to response 400 bad request`() {
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
            content { empty() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `updating value of device by forbidden null should lead to response 400 bad request`() {
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
            content { empty() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    // just to be safe that the marker is compared for identity, not equality
    fun `updating value by our 'unset' marker should use the literal string and lead to response 204 no content`() {
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
            status { isNoContent() }
            content { empty() }
        }

        verify { deviceService.findDeviceTypeById(displayId) }
        verify { deviceService.updateDevice(displayId, displayUpdate) }
    }

    @Test
    fun `updating computer data in display should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

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
            content { empty() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `updating unknown device should lead to response 404 not found`() {
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
            content {
                json(
                    """
                        {
                            "messages": [
                                "A device with id macpro-m1-95014 was not found."
                            ]
                        }
                    """
                )
            }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }
}
