/*
 * Learn more about Gradle by exploring our Samples at https://docs.gradle.org/current/samples
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("base");
    id("eclipse");
    id("idea");
    id("java");
    id("java-library");
    id("maven-publish");

    alias(libs.plugins.blossom);
    alias(libs.plugins.idea.ext);
    alias(libs.plugins.shadow);
    alias(libs.plugins.unimined);
}

val rootPackage = providers.gradleProperty("root_package").get();
val modId = providers.gradleProperty("mod_id").get();
val modName = providers.gradleProperty("mod_name").get();
val modVersion = providers.gradleProperty("mod_version").get();
val modCredits = providers.gradleProperty("mod_credits").get();
val modDescription = providers.gradleProperty("mod_description").get();
val modLogoPath = providers.gradleProperty("mod_logo_path").get();
val modUpdateJSON = providers.gradleProperty("mod_update_json").get();
val modURL = providers.gradleProperty("mod_url").get();
val coremodPluginClass = providers.gradleProperty("coremod_plugin_class").get();

val modAuthors = providers.gradleProperty("mod_authors").get().split(",").filter {
    it.isNotBlank();
}.joinToString(", ") {
    "\"${it.trim()}\"";
}

val modCompileOnly = configurations.create("modCompileOnly");
val modRuntimeOnly = configurations.create("modRuntimeOnly");

configurations.compileOnly.get().extendsFrom(modCompileOnly);
configurations.runtimeOnly.get().extendsFrom(modRuntimeOnly);

group = rootPackage;
version = modVersion;

base {
    archivesName.set(modId);
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25));
    }

    withSourcesJar();
}

repositories {
    mavenCentral();

    maven {
        url = uri("https://maven.cleanroommc.com");
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "CurseMaven";
                url = uri("https://curse.cleanroommc.com");
            }
        }

        filter {
            includeGroup("curse.maven");
        }
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth";
                url = uri("https://api.modrinth.com/maven");
            }
        }

        filter {
            includeGroup("maven.modrinth");
        }
    }

    mavenLocal(); // Must be last for caching to work
}

unimined.minecraft {
    version(libs.versions.minecraft.get());

    mappings {
        mcp(
            libs.versions.mcp.channel.get(),
            libs.versions.mcp.version.get()
        );
    }

    cleanroom {
        loader(libs.versions.cleanroom.get());

        accessTransformer(rootProject.file("src/main/resources/${modId}_at.cfg"));

        runs.all {
            systemProperty(
                "fml.coreMods.load",
                coremodPluginClass
            );
            systemProperty(
                "crl.dev.mixin",
                "${modId}.default.mixin.json,${modId}.mod.mixin.json"
            );
        }
    }

    defaultRemapJar = false;

    remap(tasks.named<Jar>("jar").get()) {
        mixinRemap {
            enableBaseMixin();
            enableMixinExtra();
        }
    }

    mods {
        remap(modCompileOnly);
        remap(modRuntimeOnly);
    }
}

dependencies {
    implementation(libs.mod.assetmover);

    testImplementation(libs.junit.jupiter);
    testRuntimeOnly(libs.junit.launcher);

    compileOnly(libs.sponge.mixin);
    compileOnly(libs.jspecify);

    "modCompileOnly"(libs.mod.fluidlogged.api);
    "modCompileOnly"(libs.mod.nothirium);
    "modRuntimeOnly"(libs.mod.fugue);
    "modRuntimeOnly"(libs.mod.scalar);

    compileOnly(fileTree("libs") { include("*.jar") });
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("package", "${rootPackage}.${modId}");
                property("mod_id", modId);
                property("mod_name", modName);
                property("mod_version", modVersion);
            }

            resources {
                property("mod_id", modId);
                property("mod_name", modName);
                property("mod_version", modVersion);
                property("mod_description", modDescription);
                property("mod_authors", modAuthors);
                property("mod_credits", modCredits);
                property("mod_url", modURL);
                property("mod_update_json", modUpdateJSON);
                property("mod_logo_path", modLogoPath);
            }
        }
    }
}

tasks.processResources {
    rename("(.+_at.cfg)", "META-INF/$1");
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25);
}

// tasks.named("shadowJar") {
//     enabled = false;
// }

tasks.named<Jar>("jar").configure {
    from(rootProject.file("LICENSE"));

    manifest {
        attributes(
            "FMLCorePlugin" to coremodPluginClass,
            "FMLCorePluginContainsFMLMod" to "true",
            "FMLAT" to "${modId}_at.cfg",
            "MixinConfigs" to "${modId}.default.mixin.json,${modId}.mod.mixin.json",
            "ModType" to "CRL"
        );
    }

    finalizedBy("remapJar");
}

tasks.named<Test>("test") {
    useJUnitPlatform();

    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25));
    });
}
