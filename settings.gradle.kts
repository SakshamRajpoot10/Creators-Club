pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "Creators Club"

// Application entry
include(":app")

// Core foundational modules
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:storage")
include(":core:navigation")

// Core engines
include(":engine:image")

// Features
include(":feature:photo")
