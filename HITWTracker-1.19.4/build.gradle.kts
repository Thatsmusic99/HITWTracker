plugins {
    id("java")
    id("fabric-loom") version("1.0-SNAPSHOT")
    id("maven-publish")
}

version = project.property("mod_version")!!
group = project.property("maven_group")!!

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings("net.fabricmc:yarn:1.19.4+build.2:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.16")

    implementation(project(":HITWTracker-Common"))
}

loom {
    accessWidenerPath.set(file("src/main/resources/hitwtracker.accesswidener"))
}

project.evaluationDependsOn(":HITWTracker-Common")

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {

    from(sourceSets.main.get().output)
    from(project(":HITWTracker-Common").sourceSets.main.get().output)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

var targetJavaVersion = 17

val minecraftVersion = "1.19.4"
val loaderVersion = "0.14.16"

tasks {

    withType<JavaCompile> {
        configureEach {
            options.encoding = "UTF-8"
            if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
                options.release.set(targetJavaVersion)
            }

            source(project(":HITWTracker-Common").sourceSets["main"].allSource)
        }
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", minecraftVersion)
        inputs.property("loader_version", loaderVersion)
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(Pair("version", project.version),
                Pair("minecraft_version", minecraftVersion),
                Pair("loader_version", loaderVersion))
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName}"}
        }
    }

    publishing {

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
        }
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    base {
        // archivesName.set(project.property("archives_base_name") as String)
    }
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}