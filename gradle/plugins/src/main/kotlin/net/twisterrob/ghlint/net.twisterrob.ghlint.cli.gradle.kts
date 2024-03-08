import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	id("org.gradle.application")
	id("net.twisterrob.ghlint.build.java")
	id("net.twisterrob.ghlint.build.kotlin")
	id("net.twisterrob.ghlint.build.testing")
	id("net.twisterrob.ghlint.build.detekt")
	id("net.twisterrob.ghlint.build.publishing")
}

kotlin {
	explicitApi = ExplicitApiMode.Strict
}

tasks.named<JavaExec>("run").configure { setWorkingDir(rootProject.layout.projectDirectory) }

val fatJar = tasks.register<Jar>("fatJar") {
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
		"META-INF/versions/9/module-info.class"
	)
	duplicatesStrategy = DuplicatesStrategy.FAIL

	doLast {
		// https://www.reddit.com/r/programminghorror/comments/3c4mtn/comment/kqt797p/
		val file = archiveFile.get().asFile
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

tasks.jar { finalizedBy(fatJar) }
