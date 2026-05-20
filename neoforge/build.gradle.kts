plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra
val soul_fire_d_version: String by extra
val flywheel_version: String by extra
val minecraft_min_version: String by extra

dependencies {
    modApi("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("maven.modrinth:sodium:mc1.21-0.6.0-beta.2-neoforge")
    modImplementation("curse.maven:farmers-delight-398521:5566383")
    //modCompileOnly("curse.maven:supplementaries-squared-838411:8120873")
    modCompileOnly("curse.maven:lucent-493280:4951434")
    modImplementation("curse.maven:farmers-delight-398521:8083481")
    modImplementation("curse.maven:etched-491890:5998004")
    modCompileOnly("curse.maven:quark-243121:5594847")
    modCompileOnly("curse.maven:zeta-968868:5597406")
    modCompileOnly("curse.maven:blueprint-382216:6505319")
    modCompileOnly("curse.maven:configured-457570:5101367")
    modCompileOnly("curse.maven:rats-323596:4802123")
    modCompileOnly("curse.maven:skinned-lanterns-414154:4656953")
    modCompileOnly("curse.maven:spelunkery-790530:5043883")
    modCompileOnly("curse.maven:scholar-961802:5214379")
    modCompileOnly("curse.maven:map-atlases-forge-519759:5307805")
    modCompileOnly("it.crystalnest:soul-fire-d-fabric:${minecraft_min_version}-${soul_fire_d_version}")
    modCompileOnly("curse.maven:puzzles-lib-495476:5330447")
    modCompileOnly("curse.maven:new-thin-air-878379:5068247")
    modCompileOnly("com.jozufozu.flywheel:flywheel-forge-${flywheel_version}")
    modCompileOnly("maven.modrinth:dye-depot:1.0.0-forge")
    modCompileOnly("curse.maven:alexs-caves-924854:4806837")
    modCompileOnly("curse.maven:citadel-331936:4786380")

    implementation("org.jetbrains:annotations:22.0.0")
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

tasks.withType<JavaCompile>().configureEach {
    val docsOut = rootProject.layout.buildDirectory.dir("docsOut").get().asFile
    options.compilerArgs.add("-Acrafttweaker.processor.document.output_directory=${docsOut.absolutePath}")
    options.compilerArgs.add("-Acrafttweaker.processor.document.multi_source=true")
}
