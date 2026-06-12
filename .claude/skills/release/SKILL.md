---
name: release
description: Release a new version of the Komapper documentation site. Performs the main-branch release (version bumps, config.toml updates, new version branch), archives the previous version branch, and updates the Netlify configuration. Use when asked to release the docs for a new Komapper version, e.g. "/release 6.2.0".
---

# Komapper docs release

Releases a new version of https://www.komapper.org/ following the process described in README.md.

**Argument**: the full Komapper version to release, e.g. `6.2.0`. If not given, ask the user.

**Derived values** (compute these up front and use them consistently):

- `NEW_BRANCH`: `v` + major.minor of the Komapper version (`6.2.0` → `v6.2`).
- `OLD_BRANCH`: the version at the top of the `[[params.versions]]` list in `config.toml` on main
  (the uncommented entry whose url is `https://www.komapper.org/`), e.g. `v6.1`.
- `OLD_SUBDOMAIN`: `OLD_BRANCH` with `.` replaced by `-`, plus the domain, e.g. `v6-1.komapper.org`.

**Netlify site**: name `komapper`, site_id `ec21695f-242f-43af-8a30-2d13a84f0637`
(the site_id also appears in the README status badge URL).

## 1. Preconditions

- Current branch is `main`, working tree is clean, and `git pull` reports up to date. Stop and ask if not.
- Check Netlify API access: `netlify api getSite --data '{"site_id": "ec21695f-242f-43af-8a30-2d13a84f0637"}'`.
  Requires the `netlify` CLI and either a logged-in session or `NETLIFY_AUTH_TOKEN`.
  If this fails, do NOT abort: continue with the git/gradle steps and collect the Netlify
  steps as manual instructions to present at the end.

## 2. Determine dependency versions

- Fetch `https://raw.githubusercontent.com/komapper/komapper/v<VERSION>/gradle/libs.versions.toml`
  and read `kotlin = "..."` and `ksp = "..."` from the `[versions]` section.
- If the tag does not exist yet, show the user what you found and ask for the Kotlin/KSP versions.
- Update `gradle.properties`: `kotlinVersion`, `kspVersion`, `komapperVersion`.

## 3. Release on main

1. Run `./gradlew prepareRelease`. This updates version references in the content files and,
   in `config.toml`: `version`, `github_branch`, and the `[[params.versions]]` list
   (inserts `NEW_BRANCH` at the top, moves `OLD_BRANCH` to its subdomain URL).
2. Review `git diff` and confirm it has the expected shape (compare with the previous
   release commit, e.g. `git show <previous "Release vX.Y" commit> --stat`):
   - `gradle.properties`: the three version numbers.
   - `config.toml`: `version` and `github_branch` are `NEW_BRANCH`; list updated as described above.
   - The six content files (en/ja Quickstart, annotation-processing, gradle-plugin) show the new versions.
   Show the user a short diff summary. If anything looks wrong, stop before committing.
3. Commit on main with message `Release <NEW_BRANCH>`, e.g. `Release v6.2`
   (same style as previous release commits).
4. Create the new branch: `git branch NEW_BRANCH`.
5. Push both: `git push origin main NEW_BRANCH`.
   Always push BEFORE switching the Netlify production branch, otherwise Netlify
   tries to build a branch that does not exist.

## 4. Switch the Netlify production branch

1. `netlify api getSite --data '{"site_id": "..."}'` — note `build_settings.repo_branch`
   (should be `OLD_BRANCH`) and `build_settings.allowed_branches`.
2. Update: set `repo_branch` to `NEW_BRANCH` and make sure `OLD_BRANCH` is in `allowed_branches`
   (so its subdomain keeps deploying as a branch deploy):

   ```
   netlify api updateSite --data '{"site_id": "...", "body": {"build_settings": {"repo_branch": "v6.2", "allowed_branches": [..existing.., "v6.1"]}}}'
   ```

3. Verify with `getSite` that the values actually changed. If the `build_settings` shape is
   rejected or has no effect, retry with `{"body": {"repo": {"repo_branch": ..., "allowed_branches": [...]}}}`.
4. If the API does not work, tell the user to do it in the Netlify UI:
   Site configuration → Build & deploy → Continuous deployment → Branches and deploy contexts.

## 5. Archive the old branch

1. `git checkout OLD_BRANCH` (pull if it tracks a remote branch).
2. Run `./gradlew archive`. This sets `archived_version = true`, `algolia_docsearch = false`,
   `offlineSearch = true`, uncomments the `latest` entry in the versions list, and moves
   `OLD_BRANCH` to its subdomain URL.
3. Review `git diff` (compare with the previous `Archive vX.Y` commit), then commit with
   message `Archive vX.Y` and push.
4. `git checkout main`.

## 6. Old-version subdomain

- After the `OLD_BRANCH` branch deploy finishes, check whether `https://OLD_SUBDOMAIN/` resolves.
- If Netlify "automatic deploy subdomains" is enabled for branch deploys, this works without
  further action. Otherwise the user must add it manually in the Netlify UI:
  Domain management → Branch subdomains → add `OLD_SUBDOMAIN` for branch `OLD_BRANCH`.

## 7. Verify end to end

1. Poll `netlify api listSiteDeploys --data '{"site_id": "..."}'` until the latest deploy for
   `NEW_BRANCH` and the latest deploy for `OLD_BRANCH` both have `"state": "ready"`
   (check every ~30s, give up after ~10 minutes and report).
2. `curl -s https://www.komapper.org/` — the page must contain `NEW_BRANCH` (version selector).
3. `curl -s https://OLD_SUBDOMAIN/` — must return the docs for `OLD_BRANCH`; ideally the
   archived-version banner is present.
4. Report a final checklist: what succeeded, what failed, and any remaining manual Netlify steps.

## Safety rules

- Never force-push.
- Never commit if the diff deviates from the expected shape — show the user first.
- If a step fails halfway, report exactly which steps are done and which remain, so the
  release can be resumed manually.
