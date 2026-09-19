# Codex handoff — CS203Event

## Decision
Actions is already used for CI; strengthen it and prepare release artifacts. Live deployment is held until the old AWS/auth dependencies are confirmed. Reviewed 2026-09-19: README and `.github/workflows/maven.yml` (blob `7ace92273fc0e55e3cff2792db670cfe0e34c407`). README describes a legacy AWS Fargate/RDS/ECS deployment and a shut-down Keycloak service. This does not establish that current endpoints or resources still exist.

The workflow runs Java 17 `mvn test` only for **development** pushes/PRs, although the repository default is main. Its test reporter uses a third-party action. Do not silently change the branch strategy without documenting which branches should be protected.

## Codex implementation
1. Record main SHA and inspect pom.xml, wrapper, test resources, security/DB configuration and any container/IaC definitions. Preserve Java version from the actual manifest. Identify Event/Order service contracts and required auth issuer behavior; no real AWS, Keycloak or payment credentials in CI.
2. Extend the existing workflow to cover the intended main/development paths, rather than add redundant CI. Use pinned actions, contents:read, bounded timeout and branch-scoped cancellable concurrency. Prefer wrapper-driven `verify` once wrapper/permissions are checked. Reporter permissions must be minimal and safe for forks; uploading JUnit results is an alternative to privileged execution of PR code.
3. Use isolated database and synthetic auth fixtures. Do not disable authentication in production to compensate for the retired Keycloak host. Test packaging and startup without contacting old external endpoints; clearly report tests that cannot run until mocks/test profiles are established.
4. Create immutable JAR/container artifacts labelled with the tested source SHA and exclude credentials/configuration secrets. Before CD, obtain an owner-confirmed inventory of the current AWS account, service/cluster, registry, DB and identity provider. Keep any release design outside active workflows until that decision is made.
5. Future AWS CD should use GitHub OIDC and a repository/branch/environment-scoped role, no static access keys, manual approval, exact digest promotion and deployment stabilization checks. Separate DB/schema changes and backup approval from image rollout. Never create a new ECS stack or public unauthenticated service as an incidental CI task.

## Acceptance
Run real Maven tests/package verification with synthetic services, actionlint and artifact inspection. Add tests for untrusted release refs, missing target/identity configuration and failed startup. Document unresolved issuer/DB/service dependencies, current test outcomes and safe rollback requirements. GitHub environment-review availability must be checked before relying on it.

Return only CI/artifact/readiness changes on this branch; no merge, live dispatch, cloud provisioning, credential access, payment calls or production changes.

References: https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws
