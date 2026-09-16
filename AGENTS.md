# Tolink

When you try to edit agents.md, stop and discuss with me.

## 1. Core Engineering Principles

### Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State your assumptions explicitly when they materially affect the solution.
- If multiple interpretations would significantly change business behavior, public interfaces, or the implementation direction, present them rather than choosing silently.
- Ask for clarification only when ambiguity materially affects business behavior, public interfaces, or the implementation direction. For low-risk, easily reversible implementation details, use the simplest interpretation consistent with the existing codebase.
- If a simpler approach exists, say so. Push back when warranted.

### Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:

- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### Browser Verification

Choose browser tooling based on the verification goal:

- Use `agent-browser` for lightweight, interactive browser checks during development, debugging, and one-off verification.
- Use Playwright when the behavior requires persistent, repeatable E2E regression coverage or integration with the project's automated test suite.
- Do not add or run browser automation when simpler verification is sufficient.

### Writing Comments

These rules apply to **every** comment you write, including ones added incidentally while fixing a bug.

- Write for a contributor reading the code at HEAD, months later, with no access to this conversation, the PR, or the diff.
- Never narrate change history ("now", "previously", "no longer") and never address the reviewer ("this correctly handles..."). State how the code works, not how it came to be or why the change is right.
- Deletion test: a comment must state something the reader cannot recover from the code. If names or types already carry it, don't write it.
- `/** */` docs state the contract (behavior, params, returns, throws); `//` comments carry rationale only. Anchor a workaround to the GitHub issue or PR that motivates it.
- When your change alters documented behavior, extend or correct the existing prose - never replace specific docs with generic text.

### Verification

