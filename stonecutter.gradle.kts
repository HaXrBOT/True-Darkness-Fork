plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.7-SNAPSHOT" apply false
    //id("dev.kikugie.j52j") version "1.0.2" apply false // Enables asset processing by writing json5 files
}
stonecutter active "1.21.2" /* [SC] DO NOT EDIT */

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


// GitHub Action Stuff
var releaseText = StringBuilder()

releaseText.append("""name: Create Release
on:
  release:
    types: [published]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: checkout repository
        uses: actions/checkout@v2

      - name: setup jdk 21
        uses: actions/setup-java@v1
        with:
          java-version: 21

      - name: make gradle wrapper executable
        run: chmod +x ./gradlew

      - name: build
        run: ./gradlew chiseledBuild
""")


var actionFile = file("$rootDir/.github/workflows/publish.yml")
if (actionFile.exists()) {
    actionFile.writeText(releaseText.toString())
}
else {
    actionFile.createNewFile()
    actionFile.writeText(releaseText.toString())
}



