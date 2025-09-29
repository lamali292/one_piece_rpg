// --------------------- Resource Processing ---------------------
import org.gradle.api.tasks.Copy

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
// --------------------- JAR Tasks ---------------------
tasks.register<Jar>("clientJar") {
    archiveBaseName.set(archivesBaseName + "-client")
    from(sourceSets["client"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcClientResources")
    from(clientResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("serverJar") {
    archiveBaseName.set(archivesBaseName + "-server")
    from(sourceSets["server"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcServerResources")
    from(serverResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// --------------------- Content JAR ---------------------
tasks.register<Jar>("contentJar") {
    archiveBaseName.set(archivesBaseName + "-content")
    from(sourceSets["content"].output.classesDirs)
    from(fileTree("src/content/resources")) {
        include("fabric.mod.json")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// --------------------- Default Build ---------------------
tasks.named("build") {
    dependsOn(
        tasks.named("clientJar"),
        tasks.named("serverJar"),
        tasks.named("contentJar")
    )
}