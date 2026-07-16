pluginManagement {
    repositories {
<<<<<<< HEAD
        google()
=======
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
        mavenCentral()
        gradlePluginPortal()
    }
}
<<<<<<< HEAD
=======
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

<<<<<<< HEAD
rootProject.name = "TaskTimerApp"
=======
rootProject.name = "Investigacion1"
>>>>>>> 5215dd0fbe865b625229fdef8e4fc15d7be3b867
include(":app")
