package com.alecarnevale.diplomatico.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

// TODO: missing tests
internal class DiplomaticoPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.dependencies.add("implementation", target.project(":api"))
    target.dependencies.add("ksp", target.project(":processors"))

    target.tasks.register("checkRoomVersions", CheckRoomVersionsTask::class.java) {
      it.group = "diplomatico"
    }
    target.tasks.named("checkRoomVersions", CheckRoomVersionsTask::class.java) {
      // TODO: release/dubug to be taken as input
      it.buildReport.convention(
        target.project.layout.buildDirectory
          .file("generated/ksp/debug/resources/com/alecarnevale/diplomatico/results/report.csv")
          .get()
          .asFile,
      )
      // TODO: path of report file tracked with git could be optionally set in input
      val assetFile =
        target.project.layout.projectDirectory
          .file("src/main/assets/diplomatico/report.csv")
          .asFile
      if (!assetFile.exists()) {
        it.assetReport.convention(
          null as File?,
        )
      } else {
        it.assetReport.convention(
          assetFile,
        )
      }
    }

    target.tasks.register("updateRoomVersions", UpdateRoomVersionsTask::class.java) {
      it.group = "diplomatico"
    }
    target.tasks.named("updateRoomVersions", UpdateRoomVersionsTask::class.java) {
      // TODO: release/dubug to be taken as input
      it.buildReport.convention(
        target.project.layout.buildDirectory
          .file("generated/ksp/debug/resources/com/alecarnevale/diplomatico/results/report.csv")
          .get()
          .asFile,
      )
      // TODO: path of report file tracked with git could be optionally set in input
      val assetFile =
        target.project.layout.projectDirectory
          .file("src/main/assets/diplomatico/report.csv")
          .asFile
      if (!assetFile.exists()) {
        assetFile.parentFile.mkdirs()
        assetFile.createNewFile()
      }
      it.assetReport.convention(
        assetFile,
      )
    }

    // TODO support debug/release
    target.tasks.named("assemble") {
      it.finalizedBy(target.tasks.named("checkRoomVersions"))
    }
  }
}
