import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("plugin.jpa") version "1.5.31"
    id("org.springframework.boot") version "2.5.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("org.postgresql:postgresql:42.2.23")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")

    implementation("org.springframework:spring-web:5.3.10")
    implementation("org.springframework:spring-webmvc:5.3.10")
    implementation("org.zalando:logbook-spring-boot-starter:2.13.0")

    implementation("org.springdoc:springdoc-openapi-ui:1.5.10")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
    // override incompatible version 1.7.2 in spring-boot-starter-test
    testRuntimeOnly("org.junit.platform:junit-platform-commons:1.8.0")

    testImplementation("io.kotest:kotest-assertions-core-jvm:4.6.3")
    testImplementation("com.ninja-squad:springmockk:3.0.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
        exclude(module = "mockito-core")
    }

    testRuntimeOnly("com.h2database:h2:1.4.200")
}

tasks.withType<KotlinCompile> {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        languageVersion = "1.5"
        apiVersion = "1.5"
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val copyOpenApiSpec = tasks.register<Copy>("copyOpenApiSpec") {
    val resourcesOutput = sourceSets.main.get().output.resourcesDir

    from(layout.projectDirectory.dir("api"))
    into(layout.buildDirectory.dir("${resourcesOutput}/static/api"))
}

tasks.processResources {
    dependsOn(copyOpenApiSpec)
}

jib {
    from {
        image = "eclipse-temurin:17.0.1_12-jdk-alpine@sha256:b30fa3ce4323ce037cb95fd2729dd4662d86f0ee2986452527cc645eaf258a1d"
    }
    to {
        image = "igorakkerman/deviceconfig-demo:latest"
    }
}

tasks.jibDockerBuild {
    dependsOn(tasks.build)
}
