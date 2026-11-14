import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService

plugins {
    // Applies the correct loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    `maven-publish`
}

// DO NOT set group = ...!
version = "${sc.properties["mod.version"]}+${sc.current.version}-fabric"
base.archivesName = sc.properties["mod.id"]

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://maven.shedaniel.me/", "Shedaniel", "me.shedaniel.cloth", "me.shedaniel", "dev.architectury")
    strictMaven("https://maven.terraformersmc.com/releases/", "TerraformersMC", "com.terraformersmc")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang Mappings on obfuscated versions
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+ - loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${sc.properties["deps.fabric_loader"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties["deps.fabric_api"]}")

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${sc.properties["deps.cloth_config"]}") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modImplementation("com.terraformersmc:modmenu:${sc.properties["deps.modmenu"]}") {
        isTransitive = false
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/langsplit.ct"),
        "build/processed.ct"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
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
    file = loomx.modJar.flatMap { it.archiveFile }
    displayName = "${sc.properties["mod.name"]} ${sc.properties["mod.version"]} for Fabric ${sc.current.version}"
    version = project.version.toString()
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")

    dryRun = providers.gradleProperty("MODRINTH_TOKEN").orNull == null
            || providers.gradleProperty("CURSEFORGE_TOKEN").orNull == null

    if (modrinthId.isNotBlank()) modrinth {
        projectId = modrinthId
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
            requires { slug = "fabric-api" }
            requires { slug = "cloth-config" }
    }

    if (curseforgeId.isNotBlank()) curseforge {
        projectId = curseforgeId
        accessToken = providers.gradleProperty("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
        client = true
        server = false
            requires { slug = "fabric-api" }
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
            groupId = "${sc.properties["mod.group"]}.fabric"
            artifactId = "${sc.properties["mod.id"]}-fabric-${sc.properties["mod.version"]}"
            version = sc.current.version

            from(components["java"])
        }
    }
}

// Uploads of all versions go to the same maven-metadata.xml - run them one at a time
tasks.withType<PublishToMavenRepository>().configureEach {
    usesService(StonecutterTaskMutexService.create(gradle, "mavenPublish"))
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
            register("loader", Regex("\\d+\\.\\d+").find(sc.properties["deps.fabric_loader"])!!.value)
            register("cloth", sc.properties["deps.cloth_config"].substringBefore('.'))
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", sc.properties["mod.version"])
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${sc.properties["mod.version"]}"))
    }
}
