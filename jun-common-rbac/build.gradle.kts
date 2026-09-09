import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":jun-common-core"))

    // spring-web 中 GenericFilterBean 的父类 EnvironmentAware 位于 spring-context，
    // 而 spring-context 在 spring-web 中为 optional，不会传递，必须显式声明
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-context")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    implementation("com.auth0:java-jwt:${property("jwt")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
