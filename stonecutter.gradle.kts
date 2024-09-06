plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.7-SNAPSHOT" apply false
    //id("dev.kikugie.j52j") version "1.0.2" apply false // Enables asset processing by writing json5 files
}
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

// Builds every version into `build/libs/{mod.version}/`
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

/*
stonecutter configureEach {
    /*
    See src/main/java/com/example/TemplateMod.java
    and https://stonecutter.kikugie.dev/
    */
    // Swaps replace the scope with a predefined value
    swap("mod_version", "\"${property("mod.version")}\";")
    // Constants add variables available in conditions
    const("release", property("mod.id") != "template")
    // Dependencies add targets to check versions against
    // Using `project.property()` in this block gets the versioned property
    dependency("fapi", project.property("deps.fabric_api").toString())
}
*/