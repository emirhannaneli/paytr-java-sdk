import java.net.URI

plugins {
    id("maven-publish")
    kotlin("jvm") version "2.0.20"
}

group = "dev.emirman.sdk"
version = "1.0.0-SNAPSHOT"

val jacksonVersion by extra("2.18.0")
val okhttpVersion by extra("4.12.0")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.getByName("kotlinSourcesJar"))
        }
    }
    repositories {
        maven {
            val rUri = URI("https://repo.emirman.dev/repository/maven-releases/")
            val sUri = URI("https://repo.emirman.dev/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) sUri else rUri
            credentials {
                username = System.getenv("REPO_USER") as String
                password = System.getenv("REPO_KEY") as String
            }
        }
    }
}