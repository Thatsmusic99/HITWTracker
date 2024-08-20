pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}
include("HITWTracker-Sheets")
include("HITWTracker-Common")
include("HITWTracker-1.20.4")
