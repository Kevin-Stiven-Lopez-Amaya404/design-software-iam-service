# Pinned contract source

`iam.yaml` and `_shared.yaml` in this directory are a **verbatim, pinned copy** of the
contracts published by the single source of truth:

- Repository: `code-sena/design-software-shared-contracts`
- Branch: `develop`
- Commit: `65f49be2c66a5564b4d8af7b40d86d0c170e961e`
- Files: `openapi/iam.yaml`, `openapi/_shared.yaml`

Do **not** hand-edit these files. To pick up a contract change:

1. Land the change upstream in `design-software-shared-contracts` (additive changes only;
   a breaking change requires a new ADR first).
2. Re-copy both files here and update the commit reference above.
3. Re-run `mvnw -pl contracts generate-sources` and fix any fallout in the adapters.

The DTOs and API interfaces consumed by `iam-adapters` are generated from these files by
`openapi-generator-maven-plugin` into `target/generated-sources/openapi`; no generated code
is committed.
