apply(from = "${project.rootDir}/constants.gradle.kts")

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

allprojects {
    group = "${property("group")}"
    version = "${property("version")}"

    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlin-spring")
    apply(plugin = "io.spring.dependency-management")

    apply(from = "${project.rootDir}/constants.gradle.kts")

    tasks.withType<JavaCompile> {
        targetCompatibility = "17"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    dependencyManagement  {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBoot")}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
apply(from = "${project.rootDir}/publish-to-maven.gradle.kts")