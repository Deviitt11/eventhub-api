# Security

This project focuses on safe defaults, minimal dependencies, and avoiding sensitive data leakage.

## Data Handling
- Do not store secrets, tokens, or credentials in code, configs, or logs.
- Avoid storing PII within the Week 1 scope (Events should not contain user PII).

## Validation & Error Safety
- Validate input using `jakarta.validation`.
- Do not leak internal stack traces in API responses.
- Use a consistent error payload with stable `code` values.

## Logging
- Avoid logging request/response bodies by default.
- Log only operationally useful metadata (path, status, correlation id if present).

## Dependency Hygiene
- Keep dependencies minimal and widely used.
- Prefer official starters.
- Review any new dependency for maintenance and security risks.

## Supply Chain / Repo Hygiene
- No secrets in commits or repository history.
- Keep `.gitignore` up to date.
- Use CI to run tests on PRs (unit + integration where configured).