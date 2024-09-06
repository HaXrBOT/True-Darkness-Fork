plugins {
    `maven-publish`
    id("fabric-loom")
    //id("dev.kikugie.j52j")
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version
val mcDep = property("mod.mc_dep").toString()
val modId = mod.id
version = "fabric-${mod.version}+mc$mcVersion"
group = mod.group
base { archivesName.set(mod.id) }

loom {
    splitEnvironmentSourceSets()

    mods {
        create("darkness") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    strictMaven("https://maven.wispforest.io", "Wisp Forest", "io.wispforest", "io.wispforest.endec")
    //maven("https://maven.wispforest.io")

    maven("https://maven.ladysnake.org/releases")
    maven("https://maven.ladysnake.org/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.shedaniel.me/")

    if (stonecutter.eval(mcVersion, "<=1.20.4")){ // Statement ensures that it only gets applied to 1.20.4 and below (Not needed above as this is in vanilla from 1.20.5 onwards)
        maven("https://maven.jamieswhiteshirt.com/libs-release")
    }
}

dependencies {
    fun fapi(vararg modules: String) = modules.forEach {
        modImplementation(fabricApi.module(it, deps["fabric_api"]))
    }

    minecraft("com.mojang:minecraft:$mcVersion")


    // mappings("net.fabricmc:yarn:$mcVersion+build.${deps["yarn_build"]}:v2")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")

    /*
    fapi(
        // Add modules from https://github.com/FabricMC/fabric
        "fabric-networking-api-v1"
    )
    */

    modImplementation("io.wispforest:owo-lib:${deps["owo_lib"]}")
    annotationProcessor("io.wispforest:owo-lib:${deps["owo_lib"]}")
    include("io.wispforest:owo-sentinel:${deps["owo_lib"]}")

    modRuntimeOnly("maven.modrinth:modmenu:${deps["mod_menu"]}")

    // Statement ensures that Apoli doesn't get applied to 1.20.6
    if (mcVersion != "1.20.6"){
        modCompileOnly("io.github.apace100:apoli:${deps["apoli"]}")
        //modRuntimeOnly("maven.modrinth:origins:${deps["origins"]}") // (Note that this causes runClient to crash upon opening the world creation screen in 1.20.1. When testing that Minecraft version, insert Origins jar manually)
    }
}

loom {
    decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(mcVersion, ">=1.20.6")) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep
    )

    filesMatching("fabric.mod.json") { expand(map) }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}