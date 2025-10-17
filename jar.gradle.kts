import org.gradle.api.tasks.Copy
import org.gradle.api.DefaultTask
import org.gradle.jvm.tasks.Jar

// --------------------- Resource Processing ---------------------
val clientResourcesDir = layout.buildDirectory.dir("processedResources/client")
val serverResourcesDir = layout.buildDirectory.dir("processedResources/server")

tasks.register<Copy>("precalcClientResources") {
    from("src/client/resources") {
        include("**/*")
    }
    from("src/main/resources") {
        include("assets/**")
        include("one_piece_api.mixins.json")
    }
    from("src/main/generated") {
        include("assets/**")
    }

    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }

    into(clientResourcesDir)
}

tasks.register<Copy>("precalcServerResources") {
    from("src/server/resources") {
        include("**/*")
    }
    from("src/main/resources") {
        include("data/**")
        include("one_piece_api.mixins.json")
        include("assets/one_piece_api/icon.png")
    }
    from("src/main/generated") {
        include("data/**")
    }

    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }

    into(serverResourcesDir)
}

val archivesBaseName: String by project
val sourceSets = project.extensions.getByName("sourceSets") as SourceSetContainer

// --------------------- JAR Tasks (Unremapped) ---------------------
val clientJarUnmapped = tasks.register<Jar>("clientJarUnmapped") {
    archiveBaseName.set(archivesBaseName + "-client")
    archiveClassifier.set("dev")
    from(sourceSets["client"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcClientResources")
    from(clientResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val serverJarUnmapped = tasks.register<Jar>("serverJarUnmapped") {
    archiveBaseName.set(archivesBaseName + "-server")
    archiveClassifier.set("dev")
    from(sourceSets["server"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcServerResources")
    from(serverResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// --------------------- Remapped JAR Tasks ---------------------
val remapClientJar = tasks.register("remapClientJar", net.fabricmc.loom.task.RemapJarTask::class.java) {
    dependsOn(clientJarUnmapped)
    inputFile.set(clientJarUnmapped.get().archiveFile)
    archiveBaseName.set(archivesBaseName + "-client")
    addNestedDependencies.set(true)
}

tasks.register<DefaultTask>("clientJar") {
    dependsOn(remapClientJar)
}

val remapServerJar = tasks.register("remapServerJar", net.fabricmc.loom.task.RemapJarTask::class.java) {
    dependsOn(serverJarUnmapped)
    inputFile.set(serverJarUnmapped.get().archiveFile)
    archiveBaseName.set(archivesBaseName + "-server")
    addNestedDependencies.set(true)
}

tasks.register<DefaultTask>("serverJar") {
    dependsOn(remapServerJar)
}

// --------------------- Content JAR ---------------------
val contentJarUnmapped = tasks.register<Jar>("contentJarUnmapped") {
    archiveBaseName.set(archivesBaseName + "-content")
    archiveClassifier.set("dev")
    from(sourceSets["content"].output.classesDirs)
    from(fileTree("src/content/resources")) {
        include("fabric.mod.json")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val remapContentJar = tasks.register("remapContentJar", net.fabricmc.loom.task.RemapJarTask::class.java) {
    dependsOn(contentJarUnmapped)
    inputFile.set(contentJarUnmapped.get().archiveFile)
    archiveBaseName.set(archivesBaseName + "-content")
    addNestedDependencies.set(true)
}

tasks.register<DefaultTask>("contentJar") {
    dependsOn(remapContentJar)
}

// --------------------- Default Build ---------------------
tasks.named("build") {
    dependsOn(
        remapClientJar,
        remapServerJar,
        remapContentJar
    )
}