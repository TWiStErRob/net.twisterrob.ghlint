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
 * [ ] action.yml validator via JSON-schema
 * [ ] action.yml semantic rules
 * [ ] support for other types (e.g. issue templates/forms)

## Usage

### Quick start

1. Copy [usage.yml](.github/workflows/usage.yml) to your repository to `.github/workflows/ghlint.yml`.
2. Change the `on:` trigger to your liking (usually `on: pull_request` or `on: push`).
3. Change the action reference to external syntax (see `TODO`).

#### Renovate

If you want to explicitly list the GH-Lint CLI version in your workflows, expand this:

<details><summary>Custom Renovate ghlint upgrade rule</summary>

If you want to separately upgrade the GH-Lint CLI version in your workflows,
specify the `version: "..."` input (inside `with:`) for the GitHub Action:
```yml
# Inside ghlint.yml in a step:

      - name: ...
        uses: ...
        with:
          version: '0.1.0' # ghlint
```

In `renovate.json` configuration file add a custom regex manager:
```json
{
	"customManagers": [
		{
			"description": "Update ghlint CLI inside GH-Lint action.",
			"customType": "regex",
			"fileMatch": ["^\\.github/workflows/ghlint\\.yml$"],
			"datasourceTemplate": "github-releases",
			"depNameTemplate": "TWiStErRob/net.twisterrob.ghlint",
			"matchStrings": [
				"version: '(?<currentValue>.*?)' # ghlint"
			],
			"extractVersionTemplate": "^v(?<version>.*)$",
			"versioningTemplate": "semver"
		}
	]
}
```

Note: The GitHub Action and the regex must match, otherwise Renovate will not see it.

</details>

### Troubleshooting

<details><summary>Advanced Security must be enabled for this repository to use code scanning.</summary>

```
Run github/codeql-action/upload-sarif@v3
  with:
    ...

RequestError [HttpError]: Advanced Security must be enabled for this repository to use code scanning.
{
    status: 403,
    response: {
        url: 'https://api.github.com/repos/<org>/<repo>/code-scanning/analysis/status',
        status: 403,
        data: {
            message: 'Advanced Security must be enabled for this repository to use code scanning.
```

https://docs.github.com/en/code-security/code-scanning/troubleshooting-code-scanning/advanced-security-must-be-enabled

</details>

## Alternatives

* [mpalmer/action-validator](https://github.com/mpalmer/action-validator)
  is a JSON schema validator for GitHub Actions including some additional checks, like glob validation.
* [yamllint](https://github.com/adrienverge/yamllint)
  is a syntax and formatting checker for YAML files.
* _If you know any others, feel free to PR._

[schemastore-workflow]: https://github.com/SchemaStore/schemastore/blob/master/src/schemas/json/github-workflow.json
