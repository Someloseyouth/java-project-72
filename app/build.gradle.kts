plugins {
    application
    checkstyle
    id("java")
    id("org.sonarqube") version "7.2.3.7755"
    id("io.freefair.lombok") version "8.13.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

application {
    mainClass.set("hexlet.code.App")
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:7.2.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

checkstyle {
    configDirectory.set(layout.projectDirectory.dir("../config/checkstyle"))
}

sonar {
    properties {
        property("sonar.projectKey", "Someloseyouth_java-project-72")
        property("sonar.organization", "someloseyouth")
    }
}

tasks.test {
    useJUnitPlatform()
}