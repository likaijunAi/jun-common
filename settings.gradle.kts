
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
include(":jun-common-upload")
include(":jun-common-autoconfigure")
include(":jun-common-starter")

include(":jun-common-upload:upload-local-file")
findProject(":jun-common-upload:upload-local-file")?.name = "upload-local-file"
include(":jun-common-upload:upload-tencent-cos")
findProject(":jun-common-upload:upload-tencent-cos")?.name = "upload-tencent-cos"
include(":jun-common-upload:upload-aws-s3")
findProject(":jun-common-upload:upload-aws-s3")?.name = "upload-aws-s3"