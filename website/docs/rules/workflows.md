# Workflow rules

Workflow rules validate GitHub Actions Workflow files (e.g. `.github/workflows/*.yml`).

Their content is loaded as `Workflow` objects.
`WorkflowVisitor` allows to granularly visit each part of the workflow.

## Example rule

See also [Rules](index.md) for general information about rules.

```kotlin
class MyWorkflowRule : VisitorRule, WorkflowVisitor {

	override val issues: List<Issue> = listOf(MyIssueId)

	override fun visitNormalJob(reporting: Reporting, job: Job.NormalJob) {
		super.visitNormalJob(reporting, job)
		if (isBad(job)) {
			reporting.report(MyIssueId, job) { "${it} has a problem." }
		}
	}

	private companion object {
		val MyIssueId = Issue(
			// ...
		)
	}
}
```
