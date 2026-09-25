/*
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds,
 * please refer to https://docs.gradle.org/current/userguide/multi_project_builds.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

pluginManagement {
    repositories {
        mavenCentral();

        gradlePluginPortal {
            content {
                excludeGroup("org.apache.logging.log4j");
            }
        }

        maven {
            name = "kikugieMavenReleases";
            url = uri("https://maven.kikugie.dev/releases");
        }
        maven {
            name = "kikugieMavenSnapshots";
            url = uri("https://maven.kikugie.dev/snapshots");
        }
        maven {
            name = "wagyourtailMavenReleases";
            url = uri("https://maven.wagyourtail.xyz/releases");
        }
        maven {
            name = "wagyourtailMavenSnapshots";
            url = uri("https://maven.wagyourtail.xyz/snapshots");
        }
        maven {
            name = "outlandsReleases";
            url = uri("https://maven.outlands.top/releases");
        }
        maven {
            name = "outlandsSnapshots";
            url = uri("https://maven.outlands.top/snapshots");
        }
        maven {
            name = "forgeMaven";
            url = uri("https://maven.minecraftforge.net/");
        }
        maven {
            name = "neoforgedReleases";
            url = uri("https://maven.neoforged.net/releases");
        }
        maven {
            name = "neoforgedSnapshots";
            url = uri("https://maven.neoforged.net/snapshots");
        }
        maven {
            url = uri("https://maven.fabricmc.net/");
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0");
}

// Due to an IntelliJ bug, this has to be done
// rootProject.name = archives_base_name
rootProject.name = rootProject.projectDir.getName();
