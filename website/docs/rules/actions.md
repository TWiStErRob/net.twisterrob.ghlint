# Action rules

Action rules validate GitHub Actions Action files (e.g. `action.yml`).

Their content is loaded as `Action` objects.
`ActionVisitor` allows to granularly visit each part of the action.

## Example rule

See also [Rules](index.md) for general information about rules.

```kotlin
class MyActionRule : VisitorRule, ActionVisitor {

	override val issues: List<Issue> = listOf(MyIssueId)

	override fun visitDockerRuns(reporting: Reporting, runs: Action.Runs.DockerRuns) {
		super.visitDockerRuns(reporting, runs)
		if (isBad(runs)) {
			reporting.report(MyIssueId, runs) { "${it} has a problem." }
		}
	}

	private companion object {
		val MyIssueId = Issue(
			// ...
		)
	}
}
```
