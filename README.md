# GitHub Actions validator

STOPSHIP it's not for syntax / expression, for that use ....

## Usage

```yaml
name: "GHA-lint"
on: push
jobs:
  gha-lint:
    steps:
      - name: "Download validator."
        run: |
          GHALINT_VERSION=0.1
          curl --silent --show-error --location --remote-name \
              https://github.com/TWiStErRob/net.twisterrob.ghlint/releases/download/v${GHALINT_VERSION}/ghlint-fat.jar

      - name: "Run validator."
        run: >
          find ".github/workflows" -type f -name "*.yml" \
            | xargs java -jar ghlint-fat.jar

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
