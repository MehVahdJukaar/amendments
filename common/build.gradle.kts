plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
    neoformVersion = "26.1.2-1"
}

val moonlight_version: String by extra
val supplementaries_version: String by extra

dependencies {
    modCompileOnly("net.mehvahdjukaar:moonlight-common:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-common:${moonlight_version}")

    compileOnly(files("/home/matteo/IdeaProjects/Supplementaries/common/build/classes/java/main"))
    //fd is fabric only for now so we compile against the local 26.1 worktree classes
    compileOnly(files("/home/matteo/IdeaProjects/FarmersDelightRefabricated-26.1/build/classes/java/main"))

    //modCompileOnly("com.jozufozu.flywheel:flywheel-forge-${flywheel_version}")
    //modCompileOnly("curse.maven:flan-404578:5290167")
    //modCompileOnly("curse.maven:etched-491890:5998004")
    //modCompileOnly("curse.maven:new-thin-air-878379:5068247")
    //modCompileOnly("curse.maven:quark-243121:5093415")
    //modCompileOnly("curse.maven:cave-enhancements-597562:4388535")
    //modCompileOnly("curse.maven:soul-fire-d-662413:6248772")
}
