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
./gradlew archive               # Archive current version for release
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
- Version numbers defined in `gradle.properties` (currently Kotlin 2.1.21, KSP 2.0.1, Komapper 5.3.0)
- `updateVersion` task automatically updates version references across documentation
- Each version gets its own branch and subdomain (e.g., v5-2.komapper.org)

### Content Structure
```
content/
├── en/              # English documentation
└── ja/              # Japanese documentation
```

## Release Process

### Main Branch Release
1. Update version numbers in `gradle.properties`
2. Run `./gradlew updateVersion`
3. Update version URLs in `config.toml`
4. Create new branch from main
5. Configure Netlify for new branch

### Archive Old Version
1. In old branch, run `./gradlew archive`
2. Update URLs in `config.toml`
3. Create subdomain on Netlify

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