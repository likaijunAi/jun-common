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

	api("com.qcloud:cos_api:${property("cosApi")}") {
		exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
		exclude(group = "com.thoughtworks.xstream", module = "xstream")
		exclude(group = "com.squareup.okio", module = "okio")
		exclude(group = "org.ini4j", module = "ini4j")
	}
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
