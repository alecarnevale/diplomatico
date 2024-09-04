package com.alecarnevale.diplomatico.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * This task check that the two report files contains the same output (name,sha) for each entity.
 */
internal abstract class CheckRoomVersionsTask : DefaultTask() {
  @get:InputFile
  abstract val buildReport: Property<File>

  @get:InputFile
  @get:Optional
  abstract val assetReport: Property<File?>

  @TaskAction
  fun check() {
    val assertReportFile = assetReport.orNull
    if (assertReportFile == null) {
      logger.error(
        "You are applying Diplomatico Gradle plugin, but no report file was found in the assets directory.\n" +
          "If you want to start monitoring the generated report file, consider run updateRoomLevels to init the asset file.",
      )
      return
    }

    val reportAreTheSame = buildReport.get().getOutputs() == assertReportFile.getOutputs()
    if (!reportAreTheSame) {
      throw DifferentReportsException()
    }
  }

  // TODO: probably not useful anymore
  private fun File.getOutputs(): Set<Output> =
    readLines()
      .map { line ->
        val record = line.split(",").let { it[0] to it[1] }
        Output(hash = record.first, qualifiedName = record.second)
      }.toSet()

  private data class Output(
    val qualifiedName: String,
    val hash: String,
  )
}
