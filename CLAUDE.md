# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Komapper Documentation project - a Hugo-based static site using the Docsy theme to document Komapper, an ORM library for server-side Kotlin supporting JDBC and R2DBC. The site is deployed at https://www.komapper.org/ and supports bilingual content (English/Japanese).

## Development Commands

```bash
# Local development (preferred method)
docker compose build
docker compose up
# Access at http://localhost:1313

# Version management
./gradlew updateVersion          # Update version numbers across documentation
./gradlew prepareRelease         # updateVersion + update the version list in config.toml
./gradlew archive               # Archive current version (config flags + version list)
./gradlew debug                 # Show current branch name

# Direct Hugo (requires local Hugo installation)
hugo server                     # Start development server
hugo server --buildDrafts      # Include draft content
```

## Architecture

### Core Components
- **Hugo**: Static site generator with Docsy theme
- **Gradle**: Version management and automated content updates
- **Docker**: Containerized development environment
- **Netlify**: Hosting with deploy previews for PRs

### Version Management System
The project maintains multiple versions with sophisticated automation:
- Version numbers defined in `gradle.properties` (Kotlin, KSP, Komapper)
- `updateVersion` task automatically updates version references across documentation
- `prepareRelease` task additionally maintains the `[[params.versions]]` list in `config.toml`
- Each version gets its own branch and subdomain (e.g., v6-0.komapper.org)

### Content Structure
```
content/
├── en/              # English documentation
└── ja/              # Japanese documentation
```

## Release Process

The release is automated by the `release` skill (`.claude/skills/release/SKILL.md`):
run `/release <komapper-version>`. The underlying steps are:

### Main Branch Release
1. Update version numbers in `gradle.properties`
2. Run `./gradlew prepareRelease` (also updates the version list in `config.toml`)
3. Commit, create new branch from main, push both
4. Switch the Netlify production branch to the new branch

### Archive Old Version
1. In old branch, run `./gradlew archive` (also updates the version list in `config.toml`)
2. Commit and push
3. Create subdomain on Netlify (unless automatic deploy subdomains are enabled)

## Important Files

- `config.toml`: Hugo configuration with version and URL settings
- `gradle.properties`: Version definitions for all dependencies
- `build.gradle.kts`: Gradle tasks for version management and content updates
- `content/en/` and `content/ja/`: Bilingual documentation content

## Development Notes

- Always maintain both English and Japanese content when making changes
- Use Gradle tasks for version updates to ensure consistency across all files
- Netlify provides automatic deploy previews for pull requests
- The Docsy theme is managed as a Hugo module dependency