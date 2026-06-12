plugins {
    base
}
val kotlinVersion: String by project
val kspVersion: String by project
val komapperVersion: String by project
val encoding: String by project
val branchName: String = "v" + komapperVersion.replace(Regex("(\\d+\\.\\d+)(\\.\\d+)(-.+)?"), "$1$3")

fun replaceVersion(version: String, prefix :String, suffix: String = "\"") {
    ant.withGroovyBuilder {
        "replaceregexp"("match" to """($prefix)[^"]*($suffix)""",
            "replace" to "\\1${version}\\2",
            "encoding" to encoding,
            "flags" to "g") {
            "fileset"("dir" to "content") {
                "include"("name" to "en/docs/Quickstart/_index.md")
                "include"("name" to "ja/docs/Quickstart/_index.md")
                "include"("name" to "en/docs/Reference/annotation-processing.md")
                "include"("name" to "ja/docs/Reference/annotation-processing.md")
                "include"("name" to "en/docs/Reference/gradle-plugin.md")
                "include"("name" to "ja/docs/Reference/gradle-plugin.md")
            }
        }
    }
}

fun changeConfig(key :String, old: String, new: String) {
    ant.withGroovyBuilder {
        "replaceregexp"("match" to "^$key = $old #can_be_replaced_with_gradle$",
            "replace" to "$key = $new #can_be_replaced_with_gradle",
            "encoding" to encoding,
            "flags" to "gm") {
            "fileset"("dir" to ".") {
                "include"("name" to "config.toml")
            }
        }
    }
}

val latestUrl = "https://www.komapper.org/"

fun subdomainUrl(version: String) = "https://" + version.replace('.', '-') + ".komapper.org/"

fun versionEntry(version: String, url: String) =
    "[[params.versions]]\nversion = \"$version\"\nurl = \"$url\""

// Insert the new version at the top of the [[params.versions]] list and
// move the previous latest version to its own subdomain URL.
fun addVersionToList() {
    val configFile = file("config.toml")
    val text = configFile.readText(charset(encoding))
    val latestEntry = Regex("""(?<!#)\[\[params\.versions]]\nversion = "(v[^"]+)"\nurl = "https://www\.komapper\.org/"""")
    val match = latestEntry.find(text)
        ?: throw GradleException("Latest version entry not found in config.toml")
    val previousVersion = match.groupValues[1]
    if (previousVersion == branchName) {
        println("config.toml already lists $branchName as the latest version")
        return
    }
    val replacement = versionEntry(branchName, latestUrl) + "\n" +
            versionEntry(previousVersion, subdomainUrl(previousVersion))
    configFile.writeText(text.replaceRange(match.range, replacement), charset(encoding))
    println("config.toml: added $branchName, moved $previousVersion to ${subdomainUrl(previousVersion)}")
}

// Uncomment the "latest" entry and point this branch's version to its subdomain URL.
fun archiveVersionList() {
    val configFile = file("config.toml")
    val text = configFile.readText(charset(encoding))
    val newText = text
        .replace(
            "#[[params.versions]]\n#version = \"latest\"\n#url = \"$latestUrl\"",
            versionEntry("latest", latestUrl))
        .replace(
            versionEntry(branchName, latestUrl),
            versionEntry(branchName, subdomainUrl(branchName)))
    if (newText == text) {
        println("config.toml: version list already archived")
    } else {
        configFile.writeText(newText, charset(encoding))
        println("config.toml: enabled latest entry, moved $branchName to ${subdomainUrl(branchName)}")
    }
}

tasks {
    register("updateVersion") {
        doLast {
            replaceVersion(kotlinVersion, """kotlin\("jvm"\) version """")
            replaceVersion(kspVersion, """id\("com.google.devtools.ksp"\) version """")
            replaceVersion(komapperVersion, """val komapperVersion = """")
            replaceVersion(komapperVersion, """id\("org.komapper.gradle"\) version """")
            changeConfig("version", "\".*\"", "\"$branchName\"")
            changeConfig("github_branch", "\".*\"", "\"$branchName\"")
        }
    }

    register("prepareRelease") {
        dependsOn("updateVersion")
        doLast {
            addVersionToList()
        }
    }

    register("archive") {
        doLast {
            changeConfig("archived_version", "false", "true")
            changeConfig("algolia_docsearch", "true", "false")
            changeConfig("offlineSearch", "false", "true")
            archiveVersionList()
        }
    }

    register("debug") {
        doLast {
            println("branchName: $branchName")
        }
    }
}
