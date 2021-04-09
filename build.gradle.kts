import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0-M2"
    kotlin("plugin.spring") version "1.5.0-M2"
    kotlin("plugin.jpa") version "1.5.0-M2"
    id("org.springframework.boot") version "2.4.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "2.8.0"
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("org.postgresql:postgresql:42.2.19")

    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.4")

    implementation("org.springframework:spring-web:5.3.6-SNAPSHOT")
    implementation("org.springframework:spring-webmvc:5.3.6-SNAPSHOT")
    implementation("org.zalando:logbook-spring-boot-starter:2.6.1")

    implementation("org.springdoc:springdoc-openapi-ui:1.5.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")

    testImplementation("io.kotest:kotest-assertions-core-jvm:4.4.3")
    testImplementation("com.ninja-squad:springmockk:3.0.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
        exclude(module = "mockito-core")
    }

    testRuntimeOnly("com.h2database:h2:1.4.200")
}

tasks.withType<KotlinCompile> {
    java {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        languageVersion = "1.5"
        apiVersion = "1.5"
        useIR = true
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jib {
    to {
        image = "igorakkerman/deviceconfig-demo:latest"
    }
}
