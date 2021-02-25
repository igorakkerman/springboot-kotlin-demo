package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.Computer
import de.igorakkerman.demo.deviceconfig.application.ComputerUpdate
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import de.igorakkerman.demo.deviceconfig.application.Display
import de.igorakkerman.demo.deviceconfig.application.DisplayUpdate
import de.igorakkerman.demo.deviceconfig.application.Resolution.WQHD
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class UpdateDevicePartiallyControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"
    private val computerUpdateFull = ComputerUpdate("best mac", "timapple", "0n3m0r3th1ng", "192.168.178.1")
    private val displayId = "samsung-screen-88276"
    private val displayUpdateFull = DisplayUpdate("second best screen", resolution = WQHD)
    private val displayUpdatePartial = DisplayUpdate("second best screen")

    @Test
    fun `update computer with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(computerId) } returns Computer::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdateFull.name}",
                    "username": "${computerUpdateFull.username}",
                    "password": "${computerUpdateFull.password}",
                    "ipAddress": "${computerUpdateFull.ipAddress}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(computerId, computerUpdateFull) }
    }

    @Test
    fun `update display with full valid data should lead to response 200 ok`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${displayUpdateFull.name}",
                    "resolution": "${displayUpdateFull.resolution}"
                }
            """
        }.andExpect {
            status { isOk() }
        }

        verify { deviceService.updateDevice(displayId, displayUpdateFull) }
    }

    @Test
    fun `update device with unknown fields should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            // id is part of the URL, not a valid updatable field
            content = """
                {
                    "id": "${displayId}",
                    "name": "${displayUpdateFull.name}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    fun `update display with computer data should lead to response 400 bad request`() {
        // given
        every { deviceService.findDeviceTypeById(displayId) } returns Display::class
        // deviceService.update(deviceId, deviceUpdate) is relaxed

        // when / then
        mockMvc.patch("/devices/$displayId") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "name": "${computerUpdateFull.name}",
                    "username": "${computerUpdateFull.username}",
                    "password": "${computerUpdateFull.password}",
                    "ipAddress": "${computerUpdateFull.ipAddress}"
                }
            """
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { deviceService.updateDevice(any(), any()) }
    }

    @Test
    @Disabled
    fun `create display with valid data should lead to response 201 created`() {
        // given
        // deviceService.createDevice(computer) is relaxed

        // when
        mockMvc.post("/devices") {
            contentType = APPLICATION_JSON
            content = """
                {
                    "type": "display",
                    "id": "${displayId}",
                    "name": "${displayUpdateFull.name}",
                    "resolution": "${displayUpdateFull.resolution}"
                }
            """
        }.andExpect {
            status { isOk() }
        }
    }

//    @Test
//    @Disabled
//    fun `create computer with existing id should lead to response 409 conflict`() {
//        // given
//        every { deviceService.createDevice(computerUpdate) } throws DeviceAreadyExistsException(computerUpdate.id)
//
//        // when/then second request with same id
//        mockMvc.post("/devices") {
//            contentType = APPLICATION_JSON
//            content = """
//                {
//                    "type": "computer",
//                    "id": "${computerUpdate.id}",
//                    "name": "${computerUpdate.name}",
//                    "username": "${computerUpdate.username}",
//                    "password": "${computerUpdate.password}",
//                    "ipAddress": "${computerUpdate.ipAddress}"
//                }
//            """
//        }.andExpect {
//            status { isConflict() }
//            content {
//                json(
//                    """
//                            {
//                                "message": "A device with id ${computerUpdate.id} already exists."
//                            }
//                    """
//                )
//            }
//        }
//    }
}
