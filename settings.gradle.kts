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

rootProject.name = "Compose-Playground"
include(":app")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:chart-engine")
include(":core:designsystem")

include(":feature:home")

include(":feature:bar-chart")

include(":feature:treemap-chart")

include(":feature:line-chart")
