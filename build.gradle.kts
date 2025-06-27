plugins {
	java
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("maven-publish")
}

fun getVersionFromFile(): String {
	val versionFile = file("./version")
	return if (versionFile.exists()) {
		versionFile.readText().trim()
	} else {
		throw GradleException("Version file not found: ${versionFile.absolutePath}")
	}
}

version = getVersionFromFile()
group = property("maven_group") as String

base {
	archivesName.set("allmusic-forked-${property("minecraft_version")}-fabric")
}

repositories {
	// 添加自定义仓库（保留原注释）
}

dependencies {
	// 版本变量来自 gradle.properties
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	// Fabric API（保留原注释）
	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21

	toolchain.languageVersion = JavaLanguageVersion.of(21)
	toolchain.vendor = JvmVendorSpec.JETBRAINS;
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}
	// 发布仓库配置（保留原注释）
}