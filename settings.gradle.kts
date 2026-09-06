pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("com.possible-triangle.helper") version ("1.4")
}

rootProject.name = "amendments"

include("common", "fabric", "neoforge")
