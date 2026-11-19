#!/usr/bin/env bash
set -euo pipefail

revision=${1:?Usage: update-schemas.sh SCHEMASTORE_COMMIT_SHA}
if [[ ! "${revision}" =~ ^[0-9a-f]{40}$ ]]; then
  echo "Expected a full SchemaStore commit SHA." >&2
  exit 1
fi

root=$(git rev-parse --show-toplevel)
target="${root}/modules/ghlint-jsonschema/src/main/resources/net/twisterrob/ghlint/yaml/schemas"
base="https://raw.githubusercontent.com/SchemaStore/schemastore/${revision}"
work_dir=$(mktemp -d)
trap 'rm -f -- "${work_dir}"/{github-workflow.json,github-action.json,LICENSE,README.md}; rmdir -- "${work_dir}"' EXIT

changed=false
for name in github-workflow.json github-action.json; do
  curl --fail --location --retry 3 --connect-timeout 10 --max-time 60 \
    "${base}/src/schemas/json/${name}" --output "${work_dir}/${name}"
  jq -e 'type == "object"' "${work_dir}/${name}" > /dev/null
  external_refs=$(jq -r '
    .. | objects | to_entries[]
    | select(.key == "$ref" or .key == "$dynamicRef" or .key == "$recursiveRef")
    | .value | select(startswith("#") | not)
  ' "${work_dir}/${name}")
  if [[ -n "${external_refs}" ]]; then
    echo "External references in ${name} need to be bundled first: ${external_refs}" >&2
    exit 1
  fi
  if ! cmp -s "${target}/${name}" "${work_dir}/${name}"; then
    changed=true
  fi
done

if [[ "${changed}" == false ]]; then
  echo "Embedded schemas are already current."
  exit 0
fi

curl --fail --location --retry 3 --connect-timeout 10 --max-time 60 \
  "${base}/LICENSE" --output "${work_dir}/LICENSE"
sed -E \
  -e "s/[0-9a-f]{40}/${revision}/g" \
  -e "s/retrieved [0-9]{4}-[0-9]{2}-[0-9]{2}/retrieved $(date -u +%F)/" \
  "${target}/README.md" > "${work_dir}/README.md"
cp -- "${work_dir}/github-workflow.json" "${work_dir}/github-action.json" \
  "${work_dir}/LICENSE" "${work_dir}/README.md" "${target}/"
