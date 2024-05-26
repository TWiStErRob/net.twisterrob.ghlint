# Usage

GH-Lint can be used in many ways.

## [CLI](cli.md)

The CLI is the main way to use GH-Lint.

Download the latest "CLI executable" from the [GitHub Releases][releases] page.

[releases]: https://github.com/TWiStErRob/net.twisterrob.ghlint/releases

It can be run from the command line as a Java application:

```console
$ java -jar ghlint.jar my-workflow.yml
```

or as an executable on Unix systems:

```console
$ ghlint my-workflow.yml
```

See [CLI documentation](cli.md) for more details.

## [GitHub Actions](gha.md)

GH-Lint can be used as a GitHub Action which downloads and runs the CLI.

```yaml
steps:
  - name: "Run GH-Lint validation."
    id: ghlint
    uses: TWiStErRob/net.twisterrob.ghlint@v0

  - name: "Publish 'GH-Lint' GitHub Code Scanning analysis."
    uses: github/codeql-action/upload-sarif@v3
    with:
      sarif_file: ${{ steps.ghlint.outputs.sarif-report }}
```

See [GitHub Actions documentation](gha.md) for more details.

## Homebrew

I wish I could say GH-Lint can be installed via Homebrew, but it's not there yet.
The [requirements for getting into the Homebrew core repository][homebrew-reqs] are quite high:

> (e.g. GitHub repositories should have >=30 forks, >=30 watchers or >=75 stars)

If you want to `brew`, you know what to do: [github.com/TWiStErRob/net.twisterrob.ghlint][repo].

[homebrew-reqs]: https://docs.brew.sh/Acceptable-Formulae#niche-or-self-submitted-stuff

[repo]: https://github.com/TWiStErRob/net.twisterrob.ghlint
