plugins {
    checkstyle
    id("java")
    id("org.sonarqube") version "7.2.3.7755"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
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