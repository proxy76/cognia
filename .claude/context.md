# Claude Code Agent Rules — Cognia Project

## Mandatory Rules

1. **Follow MVP scope strictly.** Only implement features listed in `docs/01-mvp-scope.md`. If a task touches anything in `docs/02-non-goals.md`, stop and flag it.

2. **Do not invent features.** Do not add capabilities, roles, permissions, content types, or business rules that are not in the source specification or docs.

3. **Respect architecture constraints.** Follow `docs/05-system-architecture.md`. Do not introduce microservices, change the tech stack, or add frameworks without explicit approval.

4. **Follow coding standards.** Follow `docs/09-coding-standards.md`. Business logic in services, not UI. Explicit role checks. Deterministic state transitions.

5. **Plan before coding.** For any non-trivial task, produce a plan (goal, assumptions, files, acceptance criteria) before writing code.

6. **Test all business logic.** No domain logic task is complete without tests. See `docs/11-definition-of-done.md`.

7. **List assumptions explicitly.** If you must assume something not stated in the docs, list it. If the assumption changes business rules or architecture, do not proceed — ask.

8. **Report file changes.** At the end of every task, list all files created or modified and why.

9. **No scope creep.** Do not refactor surrounding code, add docstrings to unchanged code, introduce speculative abstractions, or "improve" things that weren't asked for.

10. **No silent dependency additions.** Do not add libraries to `build.gradle.kts` or `libs.versions.toml` without stating why and getting approval.

## Source of Truth Hierarchy

1. Cognia project JSON (product spec)
2. `docs/01-mvp-scope.md`
3. `docs/02-non-goals.md`
4. `docs/05-system-architecture.md`
5. `docs/04-domain-model.md`
6. `docs/09-coding-standards.md`
7. Individual task files in `docs/tasks/`

## Task Execution Sequence

1. Read the task file and relevant source docs
2. Plan: goal, assumptions, files to change, acceptance criteria
3. Implement
4. Add tests
5. Self-review against source docs
6. Report: files changed, assumptions, limitations, follow-ups

## Forbidden Actions

- Implementing deferred/post-MVP features (battles, ELI5, comments, cards, group chat, AI moderation, monetization, parental controls)
- Changing the programming language, framework, or database without permission
- Generating placeholder code and calling it done
- Silently changing project scope
- Skipping edge cases in roles, moderation, content states, or quiz scoring
