import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    api(project(":jun-common-core"))
    api(project(":jun-common-web"))
    api(project(":jun-common-autoconfigure"))

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}