# Engineering Rules & Development Guidelines

> These rules must be followed throughout the entire development lifecycle. They apply to all AI coding agents and developers working on this project.

---

## 1. Project Understanding

- Understand the complete project requirements before writing code.
- Never make assumptions about functionality when requirements are unclear.
- Identify the purpose, target audience, core features and expected outcomes.
- Inspect the existing codebase before modifying anything.
- Understand the existing architecture, dependencies and coding conventions.
- Never overwrite or remove existing functionality without understanding its purpose.
- Ask for clarification when a requirement is ambiguous or has multiple possible interpretations.

## 2. Planning Before Implementation

- Never start coding immediately after receiving a feature request.
- Break complex requirements into smaller, manageable tasks.
- Identify dependencies between components and features.
- Prepare an implementation plan before making significant changes.
- Identify potential edge cases, technical limitations and risks.
- Consider scalability and maintainability before choosing an implementation.
- Get approval before making major architectural changes.

## 3. Architecture & Project Structure

- Follow a clean, modular and maintainable architecture.
- Maintain separation of concerns.
- Follow established design patterns where appropriate.
- Avoid unnecessary coupling between components.
- Keep business logic separate from presentation logic.
- Avoid creating excessively large files or components.
- Reuse existing utilities and components wherever possible.
- Do not introduce unnecessary abstractions or overengineering.
- Maintain consistent naming conventions throughout the project.

## 4. Coding Standards

- Write clean, readable and self-documenting code.
- Follow language-specific best practices.
- Use meaningful variable, function, class and file names.
- Keep functions small and focused on a single responsibility.
- Avoid duplicate code and unnecessary complexity.
- Avoid hardcoded configuration values.
- Use constants and environment variables where appropriate.
- Avoid unnecessary comments; document complex logic when needed.
- Remove unused imports, variables, functions and dead code.
- Never suppress errors simply to make the application work.
- Follow the existing project's formatting and linting conventions.

## 5. UI/UX Standards

- Prioritize user experience over unnecessary visual complexity.
- Maintain a consistent design system throughout the application.
- Use consistent typography, spacing, colors, borders and component styles.
- Avoid generic, repetitive or template-like interfaces.
- Every interactive element must have a meaningful purpose.
- Include appropriate hover, active, focus, loading and disabled states.
- Provide meaningful feedback for user actions.
- Include empty states, error states and success states.
- Avoid unnecessary animations and visual distractions.
- Maintain accessibility standards, including keyboard navigation and semantic HTML.
- Never use placeholder buttons or non-functional interactions in production features.

## 6. Responsive Design

- Design for mobile, tablet, laptop and desktop screens.
- Use responsive layouts instead of fixed dimensions wherever possible.
- Avoid horizontal overflow and broken layouts.
- Test components across different viewport sizes.
- Ensure navigation, forms, tables and interactive components work on mobile.
- Maintain consistent visual hierarchy across screen sizes.

## 7. Security Standards

- Never expose API keys, passwords, tokens or secrets in source code.
- Store sensitive configuration in environment variables.
- Never commit `.env` files or credentials to version control.
- Validate and sanitize user input.
- Implement proper authentication and authorization.
- Follow the principle of least privilege.
- Protect against common vulnerabilities such as SQL injection, XSS and CSRF where applicable.
- Never trust client-side validation alone.
- Avoid exposing sensitive information in logs or error responses.
- Review third-party dependencies for security implications.

## 8. Database Standards

- Use appropriate data types and constraints.
- Maintain referential integrity.
- Avoid unnecessary database duplication.
- Use indexes where they provide measurable benefits.
- Use parameterized queries or safe ORM practices.
- Maintain database migrations for schema changes.
- Never modify or delete production data without explicit authorization.
- Handle database transactions appropriately.
- Document important schema decisions.

## 9. API Development Standards

- Follow consistent REST or project-specific API conventions.
- Use appropriate HTTP methods and status codes.
- Maintain consistent request and response structures.
- Validate incoming requests.
- Implement proper authentication and authorization.
- Handle errors with meaningful responses.
- Avoid exposing internal implementation details.
- Document API endpoints and their expected parameters.
- Maintain backward compatibility when modifying existing APIs.

## 10. Error Handling & Logging

- Handle errors gracefully.
- Never silently ignore exceptions.
- Provide meaningful error messages.
- Separate user-facing errors from internal debugging information.
- Log important application events.
- Never log passwords, tokens or sensitive personal information.
- Include sufficient context in logs to identify issues.
- Avoid unnecessary console logs in production.

## 11. Performance Optimization

- Avoid unnecessary API calls and database queries.
- Optimize images and static assets.
- Implement lazy loading where appropriate.
- Avoid unnecessary component re-renders.
- Use caching when beneficial.
- Avoid loading unnecessary dependencies.
- Monitor application performance before introducing optimizations.
- Prioritize measurable improvements over premature optimization.

## 12. Testing & Debugging

- Test every significant feature after implementation.
- Verify both successful and failure scenarios.
- Test edge cases and invalid inputs.
- Fix existing issues introduced by new changes.
- Run available linting, formatting and type-checking tools.
- Run relevant unit and integration tests.
- Verify that existing features continue working after modifications.
- Never claim a feature is complete without verifying its functionality.
- Never hide failing tests to report a successful build.

## 13. Git & Version Control

- Follow a consistent branch strategy.
- Use meaningful commit messages.
- Keep commits focused on specific changes.
- Never force-push or rewrite shared history without authorization.
- Never overwrite uncommitted user changes.
- Review changes before committing.
- Keep generated files, build artifacts and secrets out of version control.
- Maintain an appropriate `.gitignore` file.

## 14. Documentation

- Maintain an updated README.
- Document project setup and installation instructions.
- Document environment variables using a safe `.env.example`.
- Document important architectural decisions.
- Document API endpoints and database structures where applicable.
- Update documentation whenever major functionality changes.
- Ensure setup instructions work on a fresh development environment.

## 15. AI Coding Agent Behaviour

The AI coding agent must follow these behavioural rules:

1. Understand before implementing.
2. Inspect before modifying.
3. Plan before making complex changes.
4. Reuse existing code before creating new implementations.
5. Never invent libraries, APIs, functions or configuration options.
6. Never remove existing functionality without permission.
7. Never introduce dependencies without a valid reason.
8. Never make unrelated changes while implementing a requested feature.
9. Explain important architectural decisions.
10. Report assumptions and limitations transparently.
11. Ask before performing destructive operations.
12. Never claim something works without testing it.
13. If an implementation fails, investigate the root cause instead of applying random fixes.
14. Keep changes focused, minimal and maintainable.
15. At the end of each task, summarize what changed, what was tested and what remains incomplete.

---

## 16. Definition of Done

A feature is considered complete only when:

- Requirements have been satisfied.
- Implementation follows project architecture.
- Code is clean and maintainable.
- UI works across supported screen sizes.
- Input validation and error handling are implemented.
- Security implications have been considered.
- Relevant tests have passed.
- No existing functionality has been unintentionally broken.
- Documentation has been updated where necessary.
- Remaining limitations are communicated.

**Final Principle:** Do not prioritize writing more code over writing correct, maintainable and reliable code. Quality, functionality and user experience take priority over development speed.
