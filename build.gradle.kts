import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "no.nav.helse.flex"
version = "1"
description = "sykepengesoknad-altinn"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

ext["okhttp3.version"] = "4.9.3" // Token-support tester trenger Mockwebserver.

val sykepengesoknadKafkaVersion = "2024.01.11-14.12-7adc3f4a"
val logstashLogbackEncoderVersion = "7.4"
val tjenestespesifikasjonerVersion = "2610.9b6de22"
val testContainersVersion = "1.19.3"
val kluentVersion = "1.73"
val tokenSupportVersion = "3.2.0"
val smCommonVersion = "2.0.8"
val gcsVersion = "2.32.1"
val gcsNioVersion = "0.127.9"
val commonsTextVersion = "1.11.0"
val jaxWsApiVersion = "1.1"
val cxfVersion = "3.5.5"
val bindApiVersion = "2.3.3"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.slf4j:slf4j-api")
    implementation("org.aspectj:aspectjrt")
    implementation("org.aspectj:aspectjweaver")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.apache.httpcomponents.client5:httpclient5")

    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")

    // https://stackoverflow.com/questions/71313332/exception-java-lang-noclassdeffounderror-javax-activation-datahandler
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("javax.xml.soap:saaj-api:1.3.5")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:3.0.3")
    // https://stackoverflow.com/questions/71095913/what-is-the-difference-between-jaxb-impl-and-jaxb-runtime
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:$bindApiVersion")
    implementation("jakarta.xml.ws:jakarta.xml.ws-api:$bindApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")

    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.helse:syfosm-common-models:$smCommonVersion")
    implementation("com.google.cloud:google-cloud-storage:$gcsVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("no.nav.helse.flex:sykepengesoknad-kafka:$sykepengesoknadKafkaVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("no.nav.tjenestespesifikasjoner:behandle-altinnmelding-v1-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:digisyfo-sykepengesoeknadarbeidsgiver:$tjenestespesifikasjonerVersion")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external:$tjenestespesifikasjonerVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:$testContainersVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.awaitility:awaitility")
    testImplementation("com.google.cloud:google-cloud-nio:$gcsNioVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")

        if (System.getenv("CI") == "true") {
            allWarningsAsErrors.set(true)
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("STARTED", "PASSED", "FAILED", "SKIPPED")
        exceptionFormat = FULL
    }
    failFast = false
}
