package net.twisterrob.ghlint.model

import java.nio.file.Path
import kotlin.io.path.name

@JvmInline
public value class FileLocation(
	public val path: String,
) {

	init {
		require(path.isNotEmpty()) { "Path must not be empty." }
	}

	public companion object
}

public val FileLocation.name: String
	get() = Path.of(path).name

public enum class FileType {
	ACTION,
	WORKFLOW,
	UNKNOWN,
}

public fun FileLocation.inferType(): FileType {
	val fileName = name.lowercase()
	return when {
		(fileName == "action.yml" || fileName == "action.yaml") && !isInGitHubWorkflows ->
			FileType.ACTION

		fileName.endsWith(".yml") || fileName.endsWith(".yaml") ->
			FileType.WORKFLOW

		else ->
			FileType.UNKNOWN
	}
}

private val FileLocation.isInGitHubWorkflows: Boolean
	get() =
		// !endsWith(".github/workflows/action.y[a]ml"), but in a way that supports running in the folder.
		Path.of(path).run { parent.parent.name == ".github" && parent.name == "workflows" }
