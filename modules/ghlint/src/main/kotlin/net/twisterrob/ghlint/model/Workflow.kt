package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.array
import net.twisterrob.ghlint.yaml.getOptional
import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequired
import net.twisterrob.ghlint.yaml.getRequiredText
import net.twisterrob.ghlint.yaml.map
import net.twisterrob.ghlint.yaml.text
import org.snakeyaml.engine.v2.nodes.MappingNode
import java.nio.file.Path
import kotlin.io.path.name

public class File internal constructor(
	public val file: FileName,
)

@JvmInline
public value class FileName(
	public val path: String,
) {

	public val name: String
		get() = Path.of(path).name
}

public class Workflow internal constructor(
	public val parent: File,
	internal val node: MappingNode,
) {

	public val name: String?
		get() = node.getOptionalText("name")

	public val jobs: Map<String, Job>
		get() = node.getRequired("jobs").map
			.mapKeys { (key, _) -> key.text }
			.mapValues { (key, value) -> Job.from(this, key, value) }

	public companion object
}

public class Job internal constructor(
	public val parent: Workflow,
	public val id: String,
	internal val node: MappingNode,
) {

	public val name: String?
		get() = node.getOptionalText("name")

	public val steps: List<Step>
		get() = node.getRequired("steps").array.map { Step.from(this, it as MappingNode) }

	public val defaults: Defaults?
		get() = node.getOptional("defaults")?.let { Defaults.from(it as MappingNode) }

	public class Defaults internal constructor(
		private val node: MappingNode,
	) {

		public val shell: String?
			get() = node.getOptionalText("shell")

		public companion object
	}

	public companion object
}

public sealed class Step protected constructor(
) {

	internal abstract val node: MappingNode
	public abstract val parent: Job

	public val name: String?
		get() = node.getOptionalText("name")

	public val id: String?
		get() = node.getOptionalText("id")

	@Suppress("detekt.VariableNaming")
	public val `if`: String?
		get() = node.getOptionalText("if")

	public data class Run internal constructor(
		public override val parent: Job,
		override val node: MappingNode,
	) : Step() {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
			get() = node.getRequiredText("run")

		public val shell: String?
			get() = node.getOptionalText("shell")
	}

	public data class Uses internal constructor(
		public override val parent: Job,
		override val node: MappingNode,
	) : Step() {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val uses: String
			get() = node.getRequiredText("uses")
	}

	public companion object
}
