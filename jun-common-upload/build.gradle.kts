import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


dependencies {
	implementation(project(":jun-common-core"))
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("com.google.code.gson:gson:${property("gson")}")
	implementation("cn.hutool:hutool-all:${property("hutool")}")
	implementation("com.auth0:java-jwt:${property("jwt")}")
	implementation("io.swagger:swagger-annotations:${property("swaggerAnnotations")}")
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
