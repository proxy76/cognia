# Coding Standards

---

## Language & Style

- **Language:** Kotlin everywhere (shared, backend, Android, web; Swift only for iOS shell)
- **Naming:** `camelCase` for functions/variables, `PascalCase` for classes/interfaces/enums, `SCREAMING_SNAKE_CASE` for constants
- **Package structure:** `com.cognia.app.<module>.<layer>` (e.g., `com.cognia.app.auth.service`)
- **File naming:** One primary class/interface per file. File name matches the class name.

## Modularization

- **Feature-based packages** within each module (auth, content, quiz, social, moderation, gamification, feed, notification, analytics)
- **Layer separation within features:** `model`, `dto`, `service`, `repository`, `route` (backend) or `ui`, `viewmodel` (client)
- Keep files focused. A file should not exceed ~300 lines. If it does, split by responsibility.

## Architecture Rules

- **Business logic must not live in UI layers.** ViewModels orchestrate; services contain logic; repositories handle data access.
- **Role and permission checks must be explicit and testable.** No implicit role assumptions buried in UI conditionals.
- **Content and moderation state transitions must be deterministic.** Use explicit state machines or when-expressions on sealed types. Every valid transition must be defined; invalid transitions must fail clearly.
- **Domain models live in `shared/`.** DTOs may also live in shared if used by both client and server.

## State Management

- Use **sealed classes/interfaces** for UI states (Loading, Success, Error, Empty).
- Use **enums or sealed hierarchies** for domain states (ContentStatus, FriendshipStatus, etc.).
- State must be **immutable** in ViewModels. Use `StateFlow` or equivalent.
- Role-dependent behavior must use explicit role checks, not flags or booleans.

## Error Handling

- Use explicit error types at API boundaries.
- Backend: return structured error responses with error codes.
- Client: map API errors to UI error states.
- Do not swallow exceptions silently. Log or propagate.
- Use `Result<T>` or similar for operations that can fail in domain/service layers.

## Dependency Control

- **Approved dependencies** are listed in `gradle/libs.versions.toml`. Do not add new dependencies without justification.
- Prefer stdlib and existing approved libraries.
- If a new dependency is needed, document the reason and get approval before adding.

## Testing

- **Unit tests** for all domain logic (services, validators, state transitions).
- **Serialization tests** for DTOs that cross API boundaries.
- **Integration tests** for backend routes where practical.
- **Permission tests** for role-based access.
- Test file location mirrors source file location with `Test` suffix.
- Use descriptive test names: `should reject license request when fewer than 5 approved videos`.

## API Design

- REST endpoints follow the contracts in `06-api-contracts.md`.
- Use cursor-based pagination for all list endpoints.
- Request/response DTOs use `kotlinx.serialization`.
- Validate all input at the API boundary.

## Git & Code Review

- Small, focused commits.
- Commit messages describe the "why", not just the "what".
- One feature/fix per branch.
- No dead code in commits.

## What NOT to Do

- Do not add comments or docs to code you did not change.
- Do not add speculative abstractions for future features.
- Do not introduce microservice boundaries without explicit instruction.
- Do not use magic strings — use constants or enums.
- Do not duplicate logic across layers.
- Do not add unused dependencies.
- Do not mix concerns in a single class (e.g., a Service that also does HTTP routing).
