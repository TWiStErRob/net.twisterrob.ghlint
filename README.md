# GitHub Actions validator

STOPSHIP it's not for syntax / expression, for that use ....

## Usage

### Quick start

1. Copy [usage.yml](.github/workflows/usage.yml) to your repository to `.github/workflows/ghlint.yml`.
2. Change the `on:` trigger to your liking (usually `on: pull_request` or `on: push`).

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
