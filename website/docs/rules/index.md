# Rules

`Rule`s check GitHub's files for specific (bad) patterns, and report them to the user.

## Implementing a Rule

A rule is implemented as a class that implements the `Rule` interface.

For typical uses it's recommended to implement `VisitorRule` instead of `Rule`,
and use one of the visitors to traverse the model.

## Types of rules

* [Workflow](workflows.md): checks the workflow files.
* [Action](actions.md): checks the action files.
* [Invalid](invalid.md): checks for invalid files.
