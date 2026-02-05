import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


dependencies {
	implementation(project(":jun-common-core"))
	implementation(project(":jun-common-upload"))
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	implementation("com.google.code.gson:gson:${property("gson")}")
	implementation("cn.hutool:hutool-all:${property("hutool")}")
}



tasks.withType<Test> {
	useJUnitPlatform()
}
