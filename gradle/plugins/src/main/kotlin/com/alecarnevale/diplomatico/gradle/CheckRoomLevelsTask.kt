package com.alecarnevale.diplomatico.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * This task check that the two report file contains the same output (sha,name) for each entity.
 */
internal abstract class CheckRoomLevelsTask : DefaultTask() {
  @TaskAction
  fun check() {
    // TODO: release/dubug to be taken as input
    val buildReport = project.layout.buildDirectory.file("generated/ksp/debug/resources/com/alecarnevale/diplomatico/results/report.csv")
    // TODO: path of report file tracked with git could be optionally set in input
    val assetReport = project.layout.projectDirectory.file("src/main/assets/diplomatico/report.csv")

    val reportAreTheSame = buildReport.get().asFile.getOutputs() == assetReport.asFile.getOutputs()
    if (!reportAreTheSame) {
      throw DifferentReportsException()
    }
  }

  private fun File.getOutputs(): Set<Output> =
    readLines()
      .map { line ->
        val record = line.split(",").let { it[0] to it[1] }
        Output(hash = record.first, qualifiedName = record.second)
      }.toSet()

  private data class Output(
    val hash: String,
    val qualifiedName: String,
  )
}
