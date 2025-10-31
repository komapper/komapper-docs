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

    register("archive") {
        doLast {
            changeConfig("archived_version", "false", "true")
            changeConfig("algolia_docsearch", "true", "false")
            changeConfig("offlineSearch", "false", "true")
        }
    }

    register("debug") {
        doLast {
            println("branchName: $branchName")
        }
    }
}
