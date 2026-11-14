import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService

plugins {
    id("net.neoforged.moddev") version "2.0.148"
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    `maven-publish`
}

version = "${sc.properties["mod.version"]}+${sc.current.version}-neoforge"
base.archivesName = sc.properties["mod.id"]

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://maven.shedaniel.me/", "Shedaniel", "me.shedaniel.cloth", "me.shedaniel", "dev.architectury")
}

dependencies {
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${sc.properties["deps.cloth_config"]}")
}

neoForge {
    version = sc.properties["deps.neo_loader"]

    mods {
        register(sc.properties["mod.id"]) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

// Release targets: Modrinth/CurseForge only when a project id is set in stonecutter.properties.toml,
// Maven (maven.rempler.com) always. Credentials come from ~/.gradle/gradle.properties.
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }
val modrinthId = sc.properties["publish.modrinth"]
val curseforgeId = sc.properties["publish.curseforge"]

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    displayName = "${sc.properties["mod.name"]} ${sc.properties["mod.version"]} for Neoforge ${sc.current.version}"
    version = project.version.toString()
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("neoforge")

    dryRun = providers.gradleProperty("MODRINTH_TOKEN").orNull == null
            || providers.gradleProperty("CURSEFORGE_TOKEN").orNull == null

    if (modrinthId.isNotBlank()) modrinth {
        projectId = modrinthId
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
            requires { slug = "cloth-config" }
    }

    if (curseforgeId.isNotBlank()) curseforge {
        projectId = curseforgeId
        accessToken = providers.gradleProperty("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
        client = true
        server = false
            requires { slug = "cloth-config" }
    }
}

publishing {
    repositories {
        maven("https://maven.rempler.com/releases") {
            name = "RemplersRepo"
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${sc.properties["mod.group"]}.neoforge"
            artifactId = "${sc.properties["mod.id"]}-neoforge-${sc.properties["mod.version"]}"
            version = sc.current.version

            from(components["java"])
        }
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
            register("cloth", sc.properties["deps.cloth_config"].substringBefore('.'))
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "*.ct", "*.classtweaker")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
        usesService(StonecutterTaskMutexService.create(gradle, name))
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", sc.properties["mod.version"])
        from(jar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${sc.properties["mod.version"]}"))
    }
}
