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
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Cambiado de FAIL_ON_PROJECT_REPOS a PREFER_SETTINGS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Senefavores"
include(":app")