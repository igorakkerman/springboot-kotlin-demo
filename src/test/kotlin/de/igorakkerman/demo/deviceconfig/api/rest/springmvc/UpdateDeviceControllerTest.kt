package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.ninjasquad.springmockk.MockkBean
import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceNotFoundException
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.DeviceType
import de.igorakkerman.demo.deviceconfig.application.DeviceUpdate
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import io.mockk.every
import io.mockk.invoke
import io.mockk.slot
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
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(computerId, capture(deviceUpdateFor)) } answers {
            // only if we invoke the callback with the correct type, the update JSON document will be parsed correctly
            deviceUpdateFor.invoke(Computer::class)
        }

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

        verify { deviceService.updateDevice(computerId, deviceUpdateFor.captured) }
    }

    @Test
    fun `updating full valid data of display should lead to response 204 no content`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            // only if we invoke the callback with the correct type, the update JSON document will be parsed correctly
            deviceUpdateFor.invoke(Display::class)
        }

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

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }


    @Test
    fun `updating partial valid data of computer should lead to response 204 no content`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(computerId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Computer::class)
        }

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

        verify { deviceService.updateDevice(computerId, deviceUpdateFor.captured) }
    }

    @Test
    fun `updating unknown fields of device should lead to response 400 bad request`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Display::class)
        }

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "id": "${displayId}",
                    "name": "${displayUpdate.name}"
                }
            """
            // 'id' is not a valid updatable field, it is part of the URL
        }.andExpect {
            status { isBadRequest() }
            // TODO: should provide error message to client
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }


    @Test
    fun `updating unknown fields of device evan by null should lead to response 400 bad request`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Display::class)
        }

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "id": null,
                    "name": "${displayUpdate.name}"
                }
            """
            // 'id' is not a valid updatable field, it is part of the URL, even updating it with 'null' is not allowed
        }.andExpect {
            status { isBadRequest() }
            // TODO: should provide error message to client
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }

    @Test
    fun `updating value of device by forbidden null should lead to response 400 bad request`() {
        // in JSON Merge Patch, null has the meaning of deletion, which is not allowed here
        // application/merge-patch+json (https://tools.ietf.org/html/rfc7396)

        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Display::class)
        }

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": null
                }
            """
            // a field cannot be updated by 'null'
        }.andExpect {
            status { isBadRequest() }
            // TODO: should provide error message to client
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }

    @Test
    // just to be safe that the UNSET marker in DeviceDocumentUpdate is compared for identity, not equality
    fun `updating value by our 'unset' marker should use the literal string and lead to response 204 no content`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Display::class)
        }

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "$UNSET"
                }
            """
            // see DeviceUpdateDocument for the meaning of UNSET
        }.andExpect {
            status { isNoContent() }
            // TODO: should provide error message to client
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }


    @Test
    fun `updating computer data in display should lead to response 400 bad request`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } answers {
            deviceUpdateFor.invoke(Display::class)
        }

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
            // TODO: should provide error message to client
            content { empty() }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }

    @Test
    fun `updating unknown device should lead to response 404 not found`() {
        // given
        val deviceUpdateFor = slot<(DeviceType) -> DeviceUpdate>()
        every { deviceService.updateDevice(displayId, capture(deviceUpdateFor)) } throws DeviceNotFoundException(displayId)

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_MERGE_PATCH_JSON
            content = """
                {
                    "name": "${displayUpdate.name}",
                }
            """
        }.andExpect {
            status { isNotFound() }
            content {
                json(
                    """
                        {
                            "messages": [
                                "A device with id samsung-screen-88276 was not found."
                            ]
                        }
                    """
                )
            }
        }

        verify { deviceService.updateDevice(displayId, deviceUpdateFor.captured) }
    }
}
