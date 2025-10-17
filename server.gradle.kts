// ----- Mod and Resourcepack for Server ------
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.Copy

// Reference the REMAPPED content jar
val contentJar = tasks.named("remapContentJar")

tasks.register<Copy>("prepareRunClientMods") {
    dependsOn(contentJar)
    from(contentJar.map { it.outputs.files })
    into("$rootDir/server/mods")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Zip>("buildResourcePack") {
    archiveFileName.set("content_assets.zip")
    destinationDirectory.set(file("$rootDir/server/resourcepacks"))

    from("$rootDir/src/content/generated/assets") {
        into("assets")
    }
    from("$rootDir/src/content/resources/assets") {
        into("assets")
    }

    doFirst {
        val packMcmeta = layout.buildDirectory.file("pack.mcmeta").get().asFile
        packMcmeta.writeText(
            """
            {
              "pack": {
                "pack_format": 17,
                "description": "One Piece Content Resource Pack"
              }
            }
        """.trimIndent()
        )
    }

    from(layout.buildDirectory.file("pack.mcmeta")) {
        into("") // root of ZIP
        rename { "pack.mcmeta" }
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    outputs.upToDateWhen { false }
}

tasks.register<Zip>("buildDatapack") {
    archiveFileName.set("content_data.zip")
    destinationDirectory.set(file("$rootDir/server/world/datapacks"))

    from("$rootDir/src/content/generated/data") {
        into("data")
    }

    doFirst {
        val packMcmeta = layout.buildDirectory.file("pack.mcmeta").get().asFile
        packMcmeta.writeText(
            """
            {
              "pack": {
                "pack_format": 18,
                "description": "One Piece Content Datapack"
              }
            }
        """.trimIndent()
        )
    }

    from(layout.buildDirectory.file("pack.mcmeta")) {
        into("") // root of ZIP
        rename { "pack.mcmeta" }
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    outputs.upToDateWhen { false }
}

tasks.register("updateDatapack") {
    dependsOn("runDatagen2")

    doLast {
        // This ensures runDatagen2 completes before the others start
    }

    finalizedBy("buildDatapack", "buildResourcePack")
}

tasks.register("prepareServer") {
    dependsOn("prepareRunClientMods", "buildResourcePack", "buildDatapack")
    group = "build"
    description = "Prepares both server mods and resource pack"
}

// --------------------- Python HTTP Server ---------------------
tasks.register("httpServer") {
    doLast {
        val resourcepacksDir = file("$rootDir/server/resourcepacks")
        if (!resourcepacksDir.exists()) {
            println("resourcepacks directory does not exist, creating...")
            resourcepacksDir.mkdirs()
        }

        val command = listOf("python", "-m", "http.server", "8000")
        println("Starting HTTP server on port 8000...")
        val process = ProcessBuilder(command)
            .directory(resourcepacksDir)
            .inheritIO()
            .start()

        // Add shutdown hook to stop the server when JVM exits
        Runtime.getRuntime().addShutdownHook(Thread {
            if (process.isAlive) {
                println("Stopping HTTP server...")
                process.destroy()
            }
        })

        project.extensions.extraProperties["httpServerProcess"] = process
        println("HTTP server http://localhost:8000 started asynchronously.")
    }
}

// Attach to server run
tasks.named("runServer") {
    dependsOn("remapContentJar")
    dependsOn("prepareServer")
    //dependsOn("httpServer")
    doLast {
        println("Minecraft server is now running while HTTP server runs in the background.")
    }
}