package de.igorakkerman.demo.deviceconfig.validation

import org.hibernate.validator.constraints.Length
import javax.validation.Constraint
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass


@MustBeDocumented
@Length(min = 4, max = 12)
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [])
annotation class Username(
    val message: String = "must be middle short",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
)

@MustBeDocumented
@Length(min = 8, max = 32)
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [])
annotation class Password(
    val message: String = "must be loooooong",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
)

@MustBeDocumented
@Pattern(
    regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$",
    message = "IPv4 address has invalid format",
)
@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [])
annotation class Ipv4Address(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<*>> = [],
)
