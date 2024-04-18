# Invalid content rules

Invalid content rules can process invalid content (e.g. `.github/workflows/*.yml` that has markdown contents).

`InvalidContentVisitor` allows to check what error was caused by loading something.

Not normally used by external rule definitions, see `ValidationRule` as an example what this is used for.

## Example rule

See also [Rules](index.md) for general information about rules.

```kotlin
class MyInvalidRule : VisitorRule, InvalidContentVisitor {

	override val issues: List<Issue> = listOf(MyIssueId)

	override fun visitInvalidContent(reporting: Reporting, content: InvalidContent) {
		super.visitInvalidContent(content, runs)
		if (isBad(content)) {
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
