import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")
}

tasks {
    getByName<BootJar>("bootJar") {
        enabled = false
    }
    getByName<Jar>("jar") {
        enabled = true
    }
}