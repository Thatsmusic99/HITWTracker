pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}
include("HITWTracker-1.20.2")
include("HITWTracker-1.20.1")
include("HITWTracker-1.19.4")
include("HITWTracker-Sheets")
include("HITWTracker-Common")
