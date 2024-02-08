package net.twisterrob.ghlint.model

public interface Rule : Visitor

public interface Visitor {

	public fun visitWorkflow(reporting: Reporting, workflow: Workflow) {}
	public fun visitJob(reporting: Reporting, job: Job) {}
	public fun visitStep(reporting: Reporting, step: Step) {}
}
