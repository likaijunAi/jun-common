import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-autoconfigure")
	implementation("org.springframework.data:spring-data-redis")
	implementation("com.google.code.gson:gson:${property("gson")}")
	implementation("cn.hutool:hutool-all:${property("hutool")}")
	implementation("de.siegmar:logback-gelf:${property("logbackGelf")}")
	implementation("com.baomidou:mybatis-plus-core:${property("mybatisPlus")}")
	implementation("com.baomidou:mybatis-plus-extension:${property("mybatisPlus")}")
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
