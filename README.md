# GitHub Actions validator

STOPSHIP it's not for syntax / expression, for that use ....

## Usage

```yaml
name: "GHA-lint"
on: push
jobs:
  gha-lint:
    name: "Validate GitHub Workflows"

    permissions:
      # actions/checkout
      contents: read
      # github/codeql-action/upload-sarif
      security-events: write
      # github/codeql-action/upload-sarif in private repositories / internal organizations.
      actions: read

    runs-on: ubuntu-latest
    steps:
      - name: "Checkout ${{ github.ref }} branch in ${{ github.repository }} repository."
        uses: actions/checkout@v4

      - name: "Set up Java."
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: temurin

      - name: "Download validator."
        run: |
          GHALINT_VERSION=0.1
          curl --silent --show-error --location --remote-name \
              https://github.com/TWiStErRob/net.twisterrob.ghlint/releases/download/v${GHALINT_VERSION}/ghlint-fat.jar

      - name: "Run validator."
        run: find ".github/workflows" -type f -name "*.yml" | xargs java -jar ghlint-fat.jar

      - name: "Upload 'GHA-lint Results' artifact."
        uses: actions/upload-artifact@v4
        with:
          name: 'GHA-lint Results'
          if-no-files-found: error
          path: |
            report.sarif

      - name: "Publish 'GHA-lint' GitHub Code Scanning analysis."
        uses: github/codeql-action/upload-sarif@v3
        with:
          checkout_path: ${{ github.workspace }}
          sarif_file: ${{ github.workspace }}/report.sarif
```

### Troubleshooting

#### Advanced Security must be enabled for this repository to use code scanning.

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
