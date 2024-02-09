package net.twisterrob.ghlint.model

import net.twisterrob.ghlint.yaml.getOptionalText
import net.twisterrob.ghlint.yaml.getRequiredText
import org.snakeyaml.engine.v2.nodes.MappingNode

public sealed class Step protected constructor(
) : InternalModel {

	public abstract val parent: Job.NormalJob

	public abstract val index: Index

	@JvmInline
	public value class Index(public val value: Int)

	public val name: String?
		get() = node.getOptionalText("name")

	public val id: String?
		get() = node.getOptionalText("id")

	@Suppress("detekt.VariableNaming")
	public val `if`: String?
		get() = node.getOptionalText("if")

	public class Run internal constructor(
		public override val parent: Job.NormalJob,
		public override val index: Index,
		override val node: MappingNode,
	) : Step() {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val run: String
			get() = node.getRequiredText("run")

		public val shell: String?
			get() = node.getOptionalText("shell")

		public companion object
	}

	public class Uses internal constructor(
		public override val parent: Job.NormalJob,
		public override val index: Index,
		override val node: MappingNode,
	) : Step() {

		@Suppress("detekt.MemberNameEqualsClassName")
		public val uses: String
			get() = node.getRequiredText("uses")

		public companion object
	}

	public companion object
}
