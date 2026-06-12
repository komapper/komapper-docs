# Komapper Documentation

[![Netlify Status](https://api.netlify.com/api/v1/badges/ec21695f-242f-43af-8a30-2d13a84f0637/deploy-status)](https://app.netlify.com/sites/komapper/deploys)

## Deployment site

https://www.komapper.org/

## Contributing to these docs

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Release steps

The release process is automated with a Claude Code skill: run `/release <komapper-version>`
(e.g. `/release 6.2.0`) in Claude Code. See [.claude/skills/release/SKILL.md](.claude/skills/release/SKILL.md).

To release manually, follow the steps below.

### in the main branch

1. Change version numbers in gradle.properties
2. Execute `./gradlew prepareRelease` (updates version references and the version list in config.toml)
3. Commit changes
4. Create a new branch from the main branch
5. Push main and the new branches to remote
6. Change the new branch to a production branch on the Netlify page

### in the old branch

1. Execute `./gradlew archive` (also updates the version list in config.toml)
2. Commit changes
3. Push the branch to remote
4. Create a new subdomain for the old branch on the Netlify page
