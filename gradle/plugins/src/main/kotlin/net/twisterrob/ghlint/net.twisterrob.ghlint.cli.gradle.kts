import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
	id("org.gradle.application")
	id("net.twisterrob.ghlint.build.java")
	id("net.twisterrob.ghlint.build.kotlin")
	id("net.twisterrob.ghlint.build.testing")
	id("net.twisterrob.ghlint.build.detekt")
}

kotlin {
	explicitApi = ExplicitApiMode.Strict
}

tasks.named<JavaExec>("run").configure { setWorkingDir(rootProject.layout.projectDirectory) }

tasks.register<Jar>("fatJar") {
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
		// TODO This makes the file polyglot and `./foo-fat.jar`-executable
		//val file = archiveFile.get().asFile
		//val bytes = file.readBytes()
		//file.writeText("#!/usr/bin/env java -jar")
		//file.appendBytes(bytes)
		//file.setExecutable(true)
	}
}
