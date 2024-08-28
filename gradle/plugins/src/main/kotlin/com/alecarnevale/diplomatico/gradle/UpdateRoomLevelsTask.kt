package com.alecarnevale.diplomatico.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * This task update the report file in the asset folder with entries contained in the build report.
 */
internal abstract class UpdateRoomLevelsTask : DefaultTask() {
  @TaskAction
  fun check() {
    // TODO: release/dubug to be taken as input
    val buildReport = project.layout.buildDirectory.file("generated/ksp/debug/resources/com/alecarnevale/diplomatico/results/report.csv")
    // TODO: path of report file tracked with git could be optionally set in input
    val assetReport = project.layout.projectDirectory.file("src/main/assets/diplomatico/report.csv")

    assetReport.asFile.writeText(buildReport.get().asFile.readText())
  }
}
