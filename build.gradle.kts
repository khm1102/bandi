plugins {
    java
    war
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "kr.ac.tukorea"
version = "0.2.1"
description = "bandi"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.5")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.security:spring-security-taglibs")
    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.22.0")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20260313.1")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api")
    implementation("org.glassfish.web:jakarta.servlet.jsp.jstl")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    providedRuntime("org.apache.tomcat.embed:tomcat-embed-jasper")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.5")
    testImplementation("org.springframework.security:spring-security-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
