
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
    }
}


apply(from = "constants.gradle.kts")

rootProject.name = "jun-common"

include(":jun-common-core")
include(":jun-common-web")
include(":jun-common-autoconfigure")
include(":jun-common-starter")
