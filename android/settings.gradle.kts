pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "Trinetra"

// App Module
include(":app")

// Core Modules
include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:ui")
include(":core:database")
include(":core:datastore")
include(":core:security")
include(":core:network")
include(":core:sync")
include(":core:analytics-engine")
include(":core:ocr")
include(":core:ai")
include(":core:speech")
include(":core:testing")

// Feature Modules (Placeholders for Phases 1+)
include(":feature:onboarding")
include(":feature:auth")
include(":feature:home")
include(":feature:vault")
include(":feature:scanner")
include(":feature:prescriptions")
include(":feature:decoder")
include(":feature:analytics")
include(":feature:care")
include(":feature:chat")
include(":feature:settings")
