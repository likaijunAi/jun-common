import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":jun-common-core"))
    implementation(project(":jun-common-web"))
    implementation("org.springframework.data:spring-data-redis")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("com.google.code.gson:gson:${property("gson")}")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("cn.hutool:hutool-all:${property("hutool")}")
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

