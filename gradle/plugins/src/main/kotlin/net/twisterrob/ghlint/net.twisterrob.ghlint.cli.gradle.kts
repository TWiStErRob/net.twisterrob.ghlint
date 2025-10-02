import net.twisterrob.ghlint.build.dsl.libs

plugins {
	id("org.gradle.application")
	id("net.twisterrob.ghlint.build.java")
	id("net.twisterrob.ghlint.build.kotlin")
	id("net.twisterrob.ghlint.build.testing")
	id("net.twisterrob.ghlint.build.detekt")
	id("net.twisterrob.ghlint.build.publishing")
}

kotlin {
	explicitApi()
}

tasks.named<JavaExec>("run").configure { setWorkingDir(rootProject.layout.projectDirectory) }

private val fatJar = tasks.register<Jar>("fatJar") {
	manifest {
		attributes(
			"Main-Class" to application.mainClass.get()
		)
	}
	archiveClassifier = "fat"
	dependsOn(configurations.runtimeClasspath)
	from(configurations.runtimeClasspath.map { it.map(::zipTree) })
	with(tasks.jar.get())
	exclude(
		// JVM metadata.
		"**/module-info.class",
		// Kotlin metadata.
		"**/*.kotlin_builtins",
		"**/*.kotlin_module",
		"**/*.kotlin_metadata",
		// Maven metadata.
		"META-INF/maven/**",
	)
	// TODEL try to revert to .FAIL after https://github.com/ajalt/mordant/pull/232 is merged.
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

private val r8Deps: Configuration = @Suppress("UnstableApiUsage") configurations.dependencyScope("r8").get()

dependencies {
	r8Deps(libs.r8)
}

private val r8: Provider<out Configuration> = configurations.resolvable("r8RuntimeClasspath") {
	extendsFrom(r8Deps)
}

private val r8Jar = tasks.register<JavaExec>("r8Jar") {
	val r8Dir = layout.buildDirectory.dir("r8")
	val r8File = r8Dir.map { it.file("minified.jar") }
	val rulesFile = layout.projectDirectory.file("src/main/r8.pro")
	val configFile = r8Dir.map { it.file("full-configuration.pro") }
	val fatJarFile = fatJar.flatMap { it.archiveFile }
	inputs.file(fatJarFile)
		.withPropertyName("fatJarFile")
		.withPathSensitivity(PathSensitivity.NONE)
	inputs.file(rulesFile)
		.withPropertyName("rulesFile")
		.withPathSensitivity(PathSensitivity.NONE)
		.normalizeLineEndings()
	outputs.file(r8File)
	outputs.file(configFile)

	javaLauncher = javaToolchains.launcherFor {
		// See https://github.com/ajalt/mordant/issues/233 why this is hardcoded.
		languageVersion = JavaLanguageVersion.of(22)
		vendor = JvmVendorSpec.ADOPTIUM // Temurin
	}

	maxHeapSize = "1G"

	classpath(r8)
	mainClass = "com.android.tools.r8.R8"
	args = listOf(
		"--release",
		"--no-minification",
		"--classfile",
		"--pg-conf", rulesFile.asFile.absolutePath,
		"--pg-conf-output", configFile.get().asFile.absolutePath,
		"--output", r8File.get().asFile.absolutePath,
		"--lib", javaLauncher.get().metadata.installationPath.asFile.absolutePath,
		fatJarFile.get().asFile.absolutePath,
	)
}

private val cliJar = tasks.register<Sync>("cliJar") {
	from(r8Jar)
	into(layout.buildDirectory.dir("cli"))
	include("*.jar")
	rename { "ghlint.jar" }
	doLast {
		// https://www.reddit.com/r/programminghorror/comments/3c4mtn/comment/kqt797p/
		val file = destinationDir.listFiles()!!.single()
		val bytes = file.readBytes()
		file.writeText("#!/bin/sh\nexec java -jar \$0 \"\$@\"\n")
		file.appendBytes(bytes)
		file.setExecutable(true)
	}
}

tasks.register("versionFile") {
	val versionFile = layout.buildDirectory.file("version.txt")
	val version = project.version
	inputs.property("version", version)
	outputs.file(versionFile).withPropertyName("versionFile")
	doLast {
		versionFile.get().asFile.writeText(version.toString())
	}
}
