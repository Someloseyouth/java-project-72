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
    implementation("com.h2database:h2:2.3.232")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("io.javalin:javalin:7.2.0")
    implementation("io.javalin:javalin-rendering-jte:7.2.0")
    implementation("gg.jte:jte:3.2.1")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("org.postgresql:postgresql:42.7.10")

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