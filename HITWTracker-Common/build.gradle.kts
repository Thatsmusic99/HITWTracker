plugins {
    id("java")
}

group = "io.github.thatsmusic99"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    compileOnly("com.google.code.gson:gson:2.10")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}