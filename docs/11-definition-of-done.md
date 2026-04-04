# Definition of Done

A task is considered **done** only when all applicable criteria below are satisfied.

---

## Documentation Tasks

- [ ] Content is accurate and consistent with the source project JSON
- [ ] No speculative features or assumptions beyond MVP scope
- [ ] All assumptions are explicitly listed
- [ ] Cross-references to other docs are correct

## Code Tasks

### Implementation
- [ ] Code compiles without errors
- [ ] Implementation matches the approved task file exactly (goal, scope, files)
- [ ] No scope creep — only what the task describes, nothing more
- [ ] Follows coding standards in `09-coding-standards.md`
- [ ] Business logic is in service/domain layer, not in UI or route handlers
- [ ] Role/permission checks are explicit and correct
- [ ] State transitions are deterministic and validated
- [ ] No new dependencies added without justification and approval

### Testing
- [ ] Unit tests cover domain logic
- [ ] Permission/role tests cover access control
- [ ] State transition tests cover content and moderation lifecycles
- [ ] Serialization tests cover DTOs (where applicable)
- [ ] All tests pass
- [ ] If tests are deferred, an explicit reason is documented in the task

### Self-Review
- [ ] Reviewed against `01-mvp-scope.md` — no out-of-scope features
- [ ] Reviewed against `02-non-goals.md` — no excluded features introduced
- [ ] Reviewed against `04-domain-model.md` — entities and enums match
- [ ] Reviewed against `05-system-architecture.md` — architecture constraints respected
- [ ] Reviewed against `09-coding-standards.md` — conventions followed
- [ ] No hallucinated assumptions — all assumptions listed and minimal
- [ ] No unnecessary complexity

### Reporting
- [ ] All created/modified files listed
- [ ] Assumptions documented
- [ ] Known limitations documented (if any)
- [ ] Follow-up tasks identified (if any)

---

## Integration Tasks

- [ ] Feature works end-to-end (API → client or API → database)
- [ ] No regressions in existing functionality
- [ ] Compatible with existing data schema

## A Task is NOT Done If:

- Tests are failing
- Implementation is partial or placeholder
- Assumptions are undocumented
- Acceptance criteria from the task file are unverified
- Scope was silently expanded
- Code was generated but not reviewed against source docs
