import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":jun-common-core"))
}


tasks.withType<Test> {
    useJUnitPlatform()
}
