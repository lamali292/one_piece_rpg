plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

version = findProperty("mod_version") as String
group = findProperty("maven_group") as String

base {
    archivesName = findProperty("archives_base_name") as String
}

repositories {
    mavenCentral()
    maven { name = "Fabric"; url = uri("https://maven.fabricmc.net/") }
    maven { name = "Modrinth"; url = uri("https://api.modrinth.com/maven") }
    maven { name = "CurseMaven"; url = uri("https://cursemaven.com") }
    maven { name = "Ladysnake Mods"; url = uri("https://maven.ladysnake.org/releases") }
    maven { name = "JitPack"; url = uri("https://jitpack.io") }
    maven { name = "KosmX"; url = uri("https://maven.kosmx.dev/") }
    maven { name = "Shedaniel"; url = uri("https://maven.shedaniel.me/") }
}

// --------------------- Source Sets ---------------------
loom {
    splitEnvironmentSourceSets()
}

val sourceSets = the<SourceSetContainer>()

sourceSets {
    val client = sourceSets.getByName("client")
    val main = sourceSets.getByName("main")
    main.resources.srcDir("src/main/generated")

    val server by creating {
        java.srcDirs("src/server/java")
        resources.srcDirs("src/server/resources")
        compileClasspath += sourceSets["main"].compileClasspath + sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].runtimeClasspath + sourceSets["main"].output
    }

    val datagen by creating {
        java.srcDirs("src/datagen/java")
        resources.srcDirs("src/datagen/resources")

        compileClasspath += main.compileClasspath + main.output
        runtimeClasspath += main.runtimeClasspath + main.output

        compileClasspath += server.compileClasspath + server.output
        runtimeClasspath += server.runtimeClasspath + server.output

        compileClasspath += client.compileClasspath + client.output
        runtimeClasspath += client.runtimeClasspath + client.output
    }

    val content by creating {
        java.srcDirs("src/content/java", "src/content/data")
        resources.srcDirs("src/content/resources", "src/content/generated")

        compileClasspath += main.compileClasspath + main.output
        runtimeClasspath += main.runtimeClasspath + main.output
        compileClasspath += server.compileClasspath + server.output
        runtimeClasspath += server.runtimeClasspath + server.output
    }
}

// --------------------- Loom Mod Setup ---------------------
loom {
    mods {
        create("one_piece_api") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
            sourceSet(sourceSets["server"])
        }

        create("one_piece_content") {
            sourceSet(sourceSets["content"])
        }
    }

    runs {
        runs.getByName("client") {
            client()
            configName = "Fabric Client"
            source(sourceSets["main"])
            source(sourceSets["client"])
            programArgs("--username", "FabricDev")
            runDir = "run"
            vmArgs("-javaagent:C:/Users/lamal/.gradle/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin/0.16.3+mixin.0.8.7/3e535042688d1265447e52ad86950b7d9678a5fa/sponge-mixin-0.16.3+mixin.0.8.7.jar")
        }

        runs.getByName("server") {
            server()
            configName = "Fabric Server"
            source(sourceSets["main"])
            source(sourceSets["server"])
            runDir = "server"
            vmArgs("-javaagent:C:/Users/lamal/.gradle/caches/modules-2/files-2.1/net.fabricmc/sponge-mixin/0.16.3+mixin.0.8.7/3e535042688d1265447e52ad86950b7d9678a5fa/sponge-mixin-0.16.3+mixin.0.8.7.jar")
        }

        create("datagen") {
            client()
            configName = "Fabric Datagen"
            runDir = "build/datagen"
            source(sourceSets["main"])
            source(sourceSets["server"])
            source(sourceSets["client"])
            source(sourceSets["datagen"])
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/main/generated")}")
            vmArg("-Dfabric-api.datagen.modid=one_piece_datagen")
        }

        create("datagen2") {
            server()
            configName = "Fabric Datagen (Content)"
            runDir = "build/datagen2"
            source(sourceSets["content"])
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/content/generated")}")
            vmArg("-Dfabric-api.datagen.modid=one_piece_content")
        }
    }
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// --------------------- Dependencies ---------------------
dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Common mods
    // Pufferfish's Skills
    modApi("curse.maven:puffish-skills-835091:6999511")

    // Spell Engine and transitive dependencies that don't get handled somehow
    modApi("maven.modrinth:spell-engine:1.7.3+1.21.1")
    modApi("maven.modrinth:spell-power:1.3.1+1.21.1")
    modRuntimeOnly("maven.modrinth:cloth-config:15.0.140+fabric")
    modRuntimeOnly("org.ladysnake.cardinal-components-api:cardinal-components-base:6.1.0")
    modRuntimeOnly("org.ladysnake.cardinal-components-api:cardinal-components-entity:6.1.0")
    modRuntimeOnly("com.github.ZsoltMolnarrr:TinyConfig:2.3.2")
    modRuntimeOnly("maven.modrinth:trinkets:3.10.0")
    modRuntimeOnly("maven.modrinth:playeranimator:2.0.0+1.21.1-fabric")

    // helpful mods for testing
    modRuntimeOnly("curse.maven:amecs-reborn-1233121:6487881")
    modRuntimeOnly("curse.maven:modmenu-308702:5810603")
}

tasks.named<ProcessResources>("processResources") {
    from(sourceSets["main"].resources.srcDirs) {
        include("fabric.mod.json")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Jar>().configureEach {
    if (name == "sourcesJar") {
        from(sourceSets["main"].allSource) {
            exclude("fabric.mod.json")
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

// --------------------- Java ---------------------
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

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

// --------------------- JAR Tasks (Unremapped) ---------------------
val clientJarUnmapped = tasks.register<Jar>("clientJarUnmapped") {
    archiveBaseName.set(findProperty("archives_base_name") as String + "-client")
    archiveClassifier.set("dev")
    from(sourceSets["client"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcClientResources")
    from(clientResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val serverJarUnmapped = tasks.register<Jar>("serverJarUnmapped") {
    archiveBaseName.set(findProperty("archives_base_name") as String + "-server")
    archiveClassifier.set("dev")
    from(sourceSets["server"].output.classesDirs)
    from(sourceSets["main"].output.classesDirs)
    dependsOn("precalcServerResources")
    from(serverResourcesDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val contentJarUnmapped = tasks.register<Jar>("contentJarUnmapped") {
    archiveBaseName.set(findProperty("archives_base_name") as String + "-content")
    archiveClassifier.set("dev")
    from(sourceSets["content"].output.classesDirs)
    from(fileTree("src/content/resources")) {
        include("fabric.mod.json")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// --------------------- Remapped JAR Tasks ---------------------
val remapClientJar = tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapClientJar") {
    dependsOn(clientJarUnmapped)
    inputFile.set(clientJarUnmapped.get().archiveFile)
    archiveBaseName.set(findProperty("archives_base_name") as String + "-client")
    addNestedDependencies.set(true)
}

val remapServerJar = tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapServerJar") {
    dependsOn(serverJarUnmapped)
    inputFile.set(serverJarUnmapped.get().archiveFile)
    archiveBaseName.set(findProperty("archives_base_name") as String + "-server")
    addNestedDependencies.set(true)
}

val remapContentJar = tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapContentJar") {
    dependsOn(contentJarUnmapped)
    inputFile.set(contentJarUnmapped.get().archiveFile)
    archiveBaseName.set(findProperty("archives_base_name") as String + "-content")
    addNestedDependencies.set(true)
}

// --------------------- Convenience Tasks ---------------------
tasks.register("clientJar") {
    dependsOn(remapClientJar)
}

tasks.register("serverJar") {
    dependsOn(remapServerJar)
}

tasks.register("contentJar") {
    dependsOn(remapContentJar)
}

// --------------------- Default Build ---------------------
tasks.named("build") {
    dependsOn(remapClientJar, remapServerJar, remapContentJar)
}

// --------------------- Server Tasks (Must be last) ---------------------
apply(from = "server.gradle.kts")