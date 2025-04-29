# Contributing to LastTimer

Thank you for your interest in contributing to LastTimer! This document provides guidelines and instructions for contributing to this project.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/LastTimer.git
   cd LastTimer
   ```
3. Set up the development environment as described in the [README.md](README.md)

## Development Workflow

1. Create a feature branch from `develop`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following our coding standards and guidelines

3. Test your changes thoroughly:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. Commit your changes with clear, descriptive messages:
   ```bash
   git commit -m "Add feature: description of your feature"
   ```

5. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

6. Submit a Pull Request against the `develop` branch of the main repository

## Code Style Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Document public APIs with KDoc comments
- Maximum line length: 100 characters
- Use 4 spaces for indentation (no tabs)

## Architecture Guidelines

- **MVVM Pattern**:
  - ViewModels should not have Android dependencies
  - UiState classes for view state representation
- **Repository Pattern**:
  - All data access through repository interfaces
  - Expose data as Flow for reactive updates
- **Dependency Injection**:
  - Use constructor injection where possible
  - Use qualifiers to disambiguate similar types

## Testing Requirements

- **Unit Tests**: Required for ViewModels and Repositories
- **UI Tests**: Recommended for key user flows
- **Test Coverage**: Aim for minimum 70% code coverage
- **TDD Approach**: Write tests before implementation where possible

## Pull Request Checklist

Before submitting your pull request, please ensure:

- [ ] Code follows style guidelines
- [ ] Tests added/updated for new functionality
- [ ] Documentation updated
- [ ] Verified on multiple API levels
- [ ] No lint warnings introduced
- [ ] Performance impact considered

## Attribution

By contributing to LastTimer, you agree that your contributions will be licensed under the project's MIT License.

## Need Help?

If you have questions or need help with the contribution process, please:
1. Check existing [Issues](https://github.com/r-bit-rry/LastTimer/issues) before creating new ones
2. Use the [Discussions](https://github.com/r-bit-rry/LastTimer/discussions) tab for general questions

For detailed information about active development tasks, bugs, and design decisions, please refer to [Ledger.md](Ledger.md).
