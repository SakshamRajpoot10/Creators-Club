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
include(":engine:canvas")
include(":engine:video")
include(":engine:audio")
include(":engine:ai")

// Features
include(":feature:photo")
include(":feature:design")
include(":feature:product")
include(":feature:drawing")
include(":feature:ar_drawing")
include(":feature:video")
include(":feature:audio")
include(":feature:writing")
include(":feature:interior")
include(":feature:ai_assistant")
include(":feature:projects")
include(":feature:settings")
