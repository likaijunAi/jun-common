apply(from = "${project.rootDir}/constants.gradle.kts")

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vanniktech.maven.publish") version "0.33.0"
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
    apply(plugin = "com.vanniktech.maven.publish")

    apply(from = "${rootDir}/constants.gradle.kts")


    tasks.withType<JavaCompile> {
        targetCompatibility = "17"
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks.named<Javadoc>("javadoc").get().destinationDir)
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBoot")}")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    mavenPublishing {
        pom {
            name.set(project.name)
            description.set("Jun Common Library - ${project.name}")
            url.set("https://github.com/likaijunAi/jun-common")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("likaijunAi")
                    name.set("likaijun")
                    email.set("l@xsocket.cn")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/likaijunAi/jun-common.git")
                developerConnection.set("scm:git:ssh://github.com/likaijunAi/jun-common.git")
                url.set("https://github.com/likaijunAi/jun-common")
            }
        }
    }

//    apply(from = "${project.rootDir}/publish-to-maven.gradle.kts")
}