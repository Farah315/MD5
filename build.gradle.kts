plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.squareup.okio:okio:3.7.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}