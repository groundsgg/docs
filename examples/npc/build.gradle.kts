plugins {
    application
    kotlin("jvm") version "2.2.20"
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://maven.pkg.github.com/groundsgg/library-npc")
                credentials {
                    username = providers.gradleProperty("github.user").orNull
                        ?: providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = providers.gradleProperty("github.token").orNull
                        ?: providers.environmentVariable("GITHUB_TOKEN").orNull
                }
            }
        }
        filter {
            includeModule("gg.grounds", "npc-core")
            includeModule("gg.grounds", "npc-minestom")
            includeModule("gg.grounds", "npc-importer")
        }
    }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
tasks.withType<JavaCompile>().configureEach { options.release.set(24) }
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) } }
sourceSets.main { java.srcDir("../../files/npc") }
dependencies {
    implementation("gg.grounds:npc-minestom:2.0.0")
    implementation("net.minestom:minestom:2026.10.07-26.2")
}
application { mainClass.set("docscheck.DocSmoke") }
tasks.named<JavaExec>("run") {
    args(layout.projectDirectory.file("../../files/npc/npc-quickstart-2.0.0-26.2.zip").asFile.absolutePath)
}

// Documentation verification only; applications loading prepared data need no importer.
val guideImporter by configurations.creating
dependencies { guideImporter("gg.grounds:npc-importer:2.0.0") }
val generatedGuides = layout.buildDirectory.dir("generated/guides")
val generateGuideSnippets by tasks.registering(Exec::class) {
    inputs.dir("../../reference/libraries/npc")
    inputs.file("../../tools/npc_guide_snippets.py")
    outputs.dir(generatedGuides)
    commandLine("python3", "../../tools/npc_guide_snippets.py", generatedGuides.get().asFile.absolutePath)
}
val guides = sourceSets.create("guides") {
    java.srcDir(generatedGuides.map { it.dir("java") })
    compileClasspath += sourceSets.main.get().output + configurations.runtimeClasspath.get() + guideImporter
}
kotlin.sourceSets.named("guides") { kotlin.srcDir(generatedGuides.map { it.dir("kotlin") }) }
tasks.named("compileGuidesJava") { dependsOn(generateGuideSnippets, tasks.classes) }
tasks.named("compileGuidesKotlin") { dependsOn(generateGuideSnippets, tasks.classes) }
tasks.register("checkSnippets") { dependsOn(tasks.named("compileGuidesJava"), tasks.named("compileGuidesKotlin")) }

// Kotlin 2.2 emits JVM24 bytecode; both checks still execute on the required Java25 runtime.
configurations.configureEach {
    attributes.attribute(org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

tasks.register<JavaExec>("checkClock") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("example.npc.AnimationHandoffCheck")
}
