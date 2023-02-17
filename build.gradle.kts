val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kgraphql_version: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    id("app.cash.sqldelight") version "2.0.0-alpha05"
}

group = "tech.harrynull"
version = "0.0.1"

application {
    mainClass.set("tech.harrynull.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    google()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.apurebase:kgraphql:$kgraphql_version")
    implementation("com.apurebase:kgraphql-ktor:$kgraphql_version")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("app.cash.sqldelight:jdbc-driver:2.0.0-alpha05")
    implementation("org.postgresql:postgresql:42.5.4")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

sqldelight {
    databases {
        create("NEIDatabase") {
            packageName.set("tech.harrynull")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0-alpha05")
        }
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "11" }

