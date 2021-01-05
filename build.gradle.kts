import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.palantir.git-version") version "0.5.2"
    `maven-publish`
}

ext {
    set("kafka_version", "2.7.0")
    set("paho_version", "1.2.5")
    set("klaxon_version", "5.0.1")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tipok/kafka-connect-mqtt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register("gpr", MavenPublication::class) {
            from(components["java"])
        }
    }
}

allprojects {
    group = "pro.tipok.kafka.connect.mqtt"
    version = "1.0.0-SNAPSHOT"

    repositories {
        jcenter()
    }

    // configure kotlin
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(platform("org.junit:junit-bom:5.7.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("io.mockk:mockk:1.10.4")
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            javaParameters = true
            jvmTarget = "1.8"
        }
    }

}

tasks.withType<ShadowJar> {
    dependencies {
        // exclude dependencies provided in the kafka connect classpath
        exclude(dependency("org.apache.kafka:connect-api:${project.ext["kafka_version"]}"))
        exclude(dependency("org.apache.kafka:kafka-clients:${project.ext["kafka_version"]}"))
        exclude(dependency("net.jpountz.lz4:.*:.*"))
        exclude(dependency("org.xerial.snappy:.*:.*"))
        exclude(dependency("org.slf4j:.*:.*"))
    }
}

dependencies {
    implementation(project(":source"))
}
