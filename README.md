# GitHub Actions validator

This is a CLI utility to **semantically** validate GitHub Actions workflows files.

## Why?
There are many [other tools](#alternatives) checking the syntax of YAML files,
or validating against the [SchemaStore github-workflow shema][schemastore-workflow],
but none of them are tailored for easily writable custom semantic rules.
This tool aims to fill the gap.

It is heavily inspired by
[detekt](https://detekt.dev/),
[PSI](https://plugins.jetbrains.com/docs/intellij/psi.html),
and [Object Calisthenics](https://www.google.com/?q=Object%20Calisthenics).

## Features
 * [x] Validate the syntax of the workflow YAML files via JSON-schema
       (this is required for other steps).
 * [x] Hand-crafted semantic rules and strongly typed object model.
 * [ ] Opinionated workflow file formatter.
 * [x] GitHub Action to make it easier to select which files.
 * [ ] Support for the full workflow syntax.
       Hopefully via generated code based on the JSON-schema.
 * [ ] Custom third-party rules (plugins)
 * [x] action.yml validator via JSON-schema
 * [x] action.yml semantic rules
 * [ ] support for other types (e.g. issue templates/forms)

## Usage

 * See [Usage documentation][usage] for a quick start.
 * See [CLI documentation][cli] for advanced usages.
 * See [GitHub Actions documentation][gha] for integration in CI.

[usage]: https://ghlint.twisterrob.net/usage/
[cli]: https://ghlint.twisterrob.net/usage/cli/
[gha]: https://ghlint.twisterrob.net/usage/gha/

## Alternatives

* [mpalmer/action-validator](https://github.com/mpalmer/action-validator)
  is a JSON schema validator for GitHub Actions including some additional checks, like glob validation.
* [yamllint](https://github.com/adrienverge/yamllint)
  is a syntax and formatting checker for YAML files.
* _If you know any others, feel free to PR._

[schemastore-workflow]: https://github.com/SchemaStore/schemastore/blob/master/src/schemas/json/github-workflow.json
