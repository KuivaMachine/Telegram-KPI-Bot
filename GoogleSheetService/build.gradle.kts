plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.github.cdimascio:java-dotenv:5.2.2")

	implementation(project(":PostgreSQL"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.kafka:spring-kafka")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	implementation("com.google.api-client:google-api-client:2.7.0")
	implementation ("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
	implementation ("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
	implementation("com.google.auth:google-auth-library-oauth2-http:1.30.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("org.springframework.kafka:spring-kafka-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-log4j2")
	modules {
		module("org.springframework.boot:spring-boot-starter-logging") {
			replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
