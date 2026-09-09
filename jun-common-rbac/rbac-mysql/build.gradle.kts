import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":jun-common-core"))
    implementation(project(":jun-common-rbac"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:${property("mybatisPlus")}")
    implementation("com.baomidou:mybatis-plus-jsqlparser:${property("mybatisPlus")}")
    implementation("com.mysql:mysql-connector-j:${property("mysqlConnector")}")
    implementation("com.google.code.gson:gson:${property("gson")}")
    implementation("cn.hutool:hutool-all:${property("hutool")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
