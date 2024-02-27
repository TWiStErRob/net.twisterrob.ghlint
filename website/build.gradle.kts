import net.twisterrob.ghlint.build.dsl.consumeGeneratedFolder

plugins {
	id("net.twisterrob.ghlint.plugin-classpath")
}

val generatedIssues = consumeGeneratedFolder(
	fromProject = projects.ghlintDocs,
	fromConfiguration = "generatedIssues",
	intoFolder = "docs/issues",
)
val generatedCliHelp = consumeGeneratedFolder(
	fromProject = projects.ghlintDocs,
	fromConfiguration = "generatedCliHelp",
	intoFolder = "docs",
)

tasks.register("build") {
	dependsOn("generateDocs")
}

tasks.register("generateDocs") {
	group = "Build"
	description = "Generate documentation"
	dependsOn(generatedIssues)
	dependsOn(generatedCliHelp)
}
