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
        }

        runs.getByName("server") {
            server()
            configName = "Fabric Server"
            source(sourceSets["main"])
            source(sourceSets["server"])
            runDir = "server"
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
            //source(sourceSets["main"])
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/content/generated")}")
            vmArg("-Dfabric-api.datagen.modid=one_piece_content")
        }
    }
}

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}



/*tasks.named("runClient") {
    dependsOn("prepareRunClientMods") // Ensure the content is copied before running
}*/


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

apply(from = "jar.gradle.kts")

// --------------------- Java ---------------------
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

apply(from = "server.gradle.kts")