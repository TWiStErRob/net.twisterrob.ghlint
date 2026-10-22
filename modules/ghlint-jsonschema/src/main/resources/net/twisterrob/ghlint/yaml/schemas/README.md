# Embedded schemas

`github-workflow.json` and `github-action.json` are copied without modification from
[SchemaStore](https://github.com/SchemaStore/schemastore/tree/5dca3c49acfef00e2d94e73493a5259b0940f834/src/schemas/json)
at commit `5dca3c49acfef00e2d94e73493a5259b0940f834` (retrieved 2026-10-22).
They are distributed under the Apache License 2.0; see the adjacent `LICENSE` file.

The `Update Embedded Schemas` workflow checks daily and opens a PR for changed schemas.
The regular pull request CI validates the update, and the repository's required checks gate auto-merge.

To update manually, run `bash .github/scripts/update-schemas.sh SCHEMASTORE_COMMIT_SHA`
from the repository root. The script validates both files, rejects external schema references,
and updates this provenance record only when the schema contents change.
