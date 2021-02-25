package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import de.igorakkerman.demo.deviceconfig.application.DeviceService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class BadRequestControllerTest(
    @Autowired
    private val mockMvc: MockMvc,
) {
    @MockkBean(relaxUnitFun = true)
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"

    @Test
    fun `request with wrong method should lead to reponse 405 method not allowed`() {
        mockMvc.post("/devices/$computerId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isMethodNotAllowed() }
        }
    }

    @Test
    fun `request with wrong 'accept' media type should lead to reponse 406 not acceptable`() {
        mockMvc.get("/devices/$computerId") {
            accept = APPLICATION_XML
            // when / then
        }.andExpect {
            status { isNotAcceptable() }
        }
    }

    @Test
    fun `request with missing body should lead to response 400 bad request`() {
        // when / then
        mockMvc.post("/devices").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `request with non-JSON body should lead to response 415 unsupported media type`() {
        // when/then
        mockMvc.post("/devices") {
            contentType = APPLICATION_XML
            content = """<computer id="ourgoodold386" />"""
        }.andExpect {
            status { isUnsupportedMediaType() }
        }
    }
}
