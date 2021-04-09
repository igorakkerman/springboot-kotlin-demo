package de.igorakkerman.demo.deviceconfig.api.rest.springmvc

import org.springframework.http.HttpHeaders.ACCEPT_PATCH
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.ContentResultMatchersDsl
import org.springframework.test.web.servlet.result.HeaderResultMatchersDsl

internal val APPLICATION_MERGE_PATCH_JSON =
    MediaType.parseMediaType(APPLICATION_MERGE_PATCH_JSON_VALUE)

internal fun HeaderResultMatchersDsl.acceptMergePatch() =
    string(ACCEPT_PATCH, APPLICATION_MERGE_PATCH_JSON_VALUE)

internal fun ContentResultMatchersDsl.empty() = string("")

