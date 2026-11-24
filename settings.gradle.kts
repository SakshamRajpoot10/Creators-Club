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

// Core foundational modules (Phase 1)
include(":core:common")
include(":core:designsystem")

// Future modules are activated as their engines and features are implemented
// include(":core:navigation")
// include(":core:database")
// include(":core:storage")
// include(":core:media")
// include(":core:permissions")
// include(":engine:image")
// include(":engine:video")
// include(":engine:audio")
// include(":engine:canvas")
// include(":engine:ai")
// include(":engine:export")
// include(":feature:home")
// include(":feature:projects")
// include(":feature:photo")
// include(":feature:design")
// include(":feature:video")
// include(":feature:audio")
// include(":feature:drawing")
// include(":feature:ar_drawing")
// include(":feature:product")
// include(":feature:interior")
// include(":feature:writing")
// include(":feature:animation")
// include(":feature:ai_assistant")
// include(":feature:documents")
// include(":feature:settings")
