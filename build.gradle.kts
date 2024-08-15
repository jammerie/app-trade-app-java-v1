plugins {
	java
	kotlin("jvm") version "2.0.10"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	runtimeOnly("com.h2database:h2")

	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")

	implementation ("com.google.code.gson:gson:2.10.1")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.register<JavaExec>("GenerateEntity") {
	group = LifecycleBasePlugin.BUILD_GROUP
	description = "Generate the Entity"
	classpath = sourceSets["main"].runtimeClasspath
	mainClass = "com.example.build.RecordGeneratorKt"
}