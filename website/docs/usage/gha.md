# GitHub Actions

There are several ways to use GH-Lint in GitHub Actions.
Each approach gives you more and more control over the validation process.

## Workflow

There's a pre-made example workflow in this repository that you can use as a starting point, this uses
the [Action](#action-public-repositories-and-github-cloud-with-ghas).

1. Copy [usage.yml][usage.yml] to your repository to `.github/workflows/ghlint.yml`.
2. Change the `on:` trigger to your liking (usually `on: pull_request` or `on: push`).
3. Change the action reference to external syntax (see `TODO`).
4. Remove the `if:` condition on the job, it's specific to this repository only.

[usage.yml]: https://github.com/TWiStErRob/net.twisterrob.ghlint/blob/main/.github/workflows/usage.yml

## Action

### Public repositories / GitHub Cloud with GHAS

The repository that hosts the code can be referenced in a GitHub Actions workflow as an action in a step.

The minimal usage looks like this:

```yaml
steps:
  - name: "Checkout ${{ github.ref }} in ${{ github.repository }} repository."
    uses: actions/checkout@v4

  - name: "Run GH-Lint validation."
    id: ghlint
    uses: TWiStErRob/net.twisterrob.ghlint@v0

  - name: "Publish 'GH-Lint' GitHub Code Scanning analysis."
    uses: github/codeql-action/upload-sarif@v3
    with:
      sarif_file: ${{ steps.ghlint.outputs.sarif-report }}
```

See the [action.yml][action.yml] for inputs and outputs.

[action.yml]: https://github.com/TWiStErRob/net.twisterrob.ghlint/blob/main/action.yml

### Private repositories / GitHub Cloud without GHAS

If you get this error message:
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

</details>

it means that you don't have enough permissions to use GHAS (GitHub Advanced Security) feature.
See [docs](https://docs.github.com/en/code-security/code-scanning/troubleshooting-code-scanning/advanced-security-must-be-enabled)
for more details.

You can still use the GH-Lint Action, but you won't be able to publish the SARIF report to GitHub.

```yaml
  - name: "Checkout ${{ github.ref }} in ${{ github.repository }} repository."
    uses: actions/checkout@v4

  - name: "Download GH-Lint."
    working-directory: ${{ runner.temp }}
    env:
      GHLINT_VERSION: '0.5.0'
      GH_TOKEN: ${{ github.token }}
    run: gh release download --repo "TWiStErRob/net.twisterrob.ghlint" "v${GHLINT_VERSION}" --pattern "ghlint.jar"

  - name: "Run GH-Lint validation."
    run: java -jar ${RUNNER_TEMP}/ghlint.jar --exit --ghcommands .github/workflows/*.yml
```

This will fail your workflow if there are any findings.
The findings can be found in the workflow logs,
but also shown on the workflow run summary and pull request as annotations.

## Renovate

By default, renovate will update all GitHub Actions actions to the latest version.

In addition to that, Renovate can also maintain the other approaches with a bit of configuration.
In `renovate.json` configuration file add a custom regex manager as shown below.

### Explicit version

If you want to independently set the version of the GH-Lint CLI in your action usage.

```yaml
  - name: "Run GH-Lint validation."
    uses: TWiStErRob/net.twisterrob.ghlint@v0
    with:
      version: '0.5.0' # ghlint
```

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

### Explicit download

If you want to set the version of the GH-Lint CLI (JAR to download) in your workflows.

```yaml
    env:
      GHLINT_VERSION: '0.5.0'
```

```json
{
	"customManagers": [
		{
			"description": "Update ghlint CLI from GitHub Releases.",
			"customType": "regex",
			"fileMatch": ["^\\.github/workflows/my-workflow\\.yml$"],
			"datasourceTemplate": "github-releases",
			"depNameTemplate": "TWiStErRob/net.twisterrob.ghlint",
			"matchStrings": [
				"GHLINT_VERSION: '(?<currentValue>.*?)'"
			],
			"extractVersionTemplate": "^v(?<version>.*)$",
			"versioningTemplate": "semver"
		}
	]
}
```
