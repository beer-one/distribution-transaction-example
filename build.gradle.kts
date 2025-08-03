import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("org.springframework.boot") version "3.4.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.cloud.tools.jib") version "3.3.1"

    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    kotlin("kapt") version "1.8.21"
    kotlin("plugin.jpa") version "1.8.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

ext["spring.version"] = "3.4.8"
val springCloudVersion = "2024.0.2"

// @see https://github.com/Kotlin/kotlinx.coroutines/tree/1.7.3?tab=readme-ov-file#using-in-your-projects
val coroutineVersion = "1.7.3"

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "kotlin-kapt")
    apply(plugin = "kotlin-spring")
    apply(plugin = "kotlin-jpa")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.springframework.boot:spring-boot-starter")
        implementation("org.springframework.boot:spring-boot-starter-webflux")

        // jpa & querydsl
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        kapt("org.springframework.boot:spring-boot-configuration-processor")

        runtimeOnly("com.mysql:mysql-connector-j")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib")

        // Kafka
        implementation("org.springframework.kafka:spring-kafka")
        implementation("io.projectreactor.kafka:reactor-kafka")

        // feign
        implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

        // coroutine
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutineVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${coroutineVersion}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${coroutineVersion}")

        // actuator & monitoring
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-registry-prometheus")
        implementation("io.micrometer:micrometer-core")
    }

    if (project.name != "core") {
        apply(plugin = "com.google.cloud.tools.jib")
        dependencies {
            implementation(project(":core"))
        }
    }
}