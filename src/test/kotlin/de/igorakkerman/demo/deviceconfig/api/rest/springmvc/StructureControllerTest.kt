package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import com.ninjasquad.springmockk.MockkBean
import de.igorakkerman.demo.deviceconfig.application.DeviceService
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_XML
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.options
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@WebMvcTest(controllers = [DeviceController::class])
@ContextConfiguration(classes = [DeviceController::class])
class StructureControllerTest(
    @Autowired
    private val mockMvc: MockMvc
) {
    @MockkBean(relaxUnitFun = true)
    @Suppress("unused") // avoid wiring service and repository
    private lateinit var deviceService: DeviceService

    private val computerId = "macpro-m1-95014"


    @Test
    fun `OPTIONS root path request should return correct Allow methods`() {
        mockMvc.options("/devices") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            header {
                string(
                    HttpHeaders.ALLOW, CoreMatchers.allOf(
                        containsString("GET"),
                        containsString("HEAD"),
                        containsString("POST"),
                        containsString("OPTIONS"),
                    )
                )
            }
            content { empty() }
        }
    }

    @Test
    fun `OPTIONS id path request should return correct Allow methods and correct Accept-Patch`() {
        mockMvc.options("/devices/$computerId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNoContent() }
            header {
                string(
                    HttpHeaders.ALLOW, CoreMatchers.allOf(
                        containsString("GET"),
                        containsString("HEAD"),
                        containsString("PUT"),
                        containsString("PATCH"),
                        containsString("OPTIONS"),
                    )
                )
                header { acceptMergePatch() }
                content { empty() }
            }
        }
    }

    @Test
    fun `POST id path request should lead to reponse 405 method not allowed`() {
        mockMvc.post("/devices/$computerId") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isMethodNotAllowed() }
            content { empty() }
        }
    }

    @Test
    fun `PUT root path request should lead to reponse 405 method not allowed`() {
        mockMvc.put("/devices") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isMethodNotAllowed() }
            content { empty() }
        }
    }

    @Test
    fun `PATCH root path request method should lead to reponse 405 method not allowed`() {
        mockMvc.patch("/devices") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isMethodNotAllowed() }
            content { empty() }
        }
    }

    @Test
    fun `GET request with wrong 'accept' media type should lead to reponse 406 not acceptable`() {
        mockMvc.get("/devices/$computerId") {
            accept = APPLICATION_XML
            // when / then
        }.andExpect {
            status { isNotAcceptable() }
            content { empty() }
        }
    }

    @Test
    fun `GET all request with wrong 'accept' media type should lead to reponse 406 not acceptable`() {
        mockMvc.get("/devices") {
            accept = APPLICATION_XML
            // when / then
        }.andExpect {
            status { isNotAcceptable() }
            content { empty() }
        }
    }

    @Test
    // when / then
    fun `PATCH request with empty body should lead to response 400 unsupported media type`() {
        mockMvc.patch("/devices/$computerId").andExpect {
            status { isUnsupportedMediaType() }
            header { acceptMergePatch() }
            content { empty() }
        }
    }

    @Test
    fun `PATCH request with non-JSON-merge-patch body should lead to response 415 unsupported media type`() {
        mockMvc.patch("/devices/$computerId") {
            contentType = APPLICATION_JSON
            content = """{"name": "cool name"}"""
        }.andExpect {
            status { isUnsupportedMediaType() }
            header { acceptMergePatch() }
            content { empty() }
        }
    }

    @Test
    fun `POST request with empty body should lead to response 415 unsupported media type`() {
        // when / then
        mockMvc.post("/devices").andExpect {
            status { isUnsupportedMediaType() }
            content { empty() }
        }
    }

    @Test
    fun `POST request with non-JSON body should lead to response 415 unsupported media type`() {
        // when/then
        mockMvc.post("/devices") {
            contentType = APPLICATION_XML
            content = """<computer id="ourgoodold386" />"""
        }.andExpect {
            status { isUnsupportedMediaType() }
            content { empty() }
        }
    }
}
