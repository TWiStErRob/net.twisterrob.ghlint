package net.twisterrob.ghlint.model

public interface Reporting {

	public fun report(issue: Issue, context: Any)
	public fun putState(rule: Rule, key: String, value: Any?)
	public fun getState(rule: Rule, key: String): Any?
}
