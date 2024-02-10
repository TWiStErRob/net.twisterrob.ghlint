package net.twisterrob.ghlint.model

public interface Workflow : Model {

	public val parent: File
	public val name: String?
	public val env: Map<String, String>?
	public val jobs: Map<String, Job>
	public val permissions: Map<String, String>?

	public companion object
}

public val Workflow.id: String
	get() = parent.file.name
