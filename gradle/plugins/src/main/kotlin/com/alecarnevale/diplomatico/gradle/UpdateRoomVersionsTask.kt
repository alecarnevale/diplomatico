package com.alecarnevale.diplomatico.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * This task update the report file in the asset folder with entries contained in the build report.
 */
internal abstract class UpdateRoomVersionsTask : DefaultTask() {
  @get:InputFile
  abstract val buildReport: Property<File>

  @get:InputFile
  abstract val assetReport: Property<File>

  @TaskAction
  fun update() {
    assetReport.get().writeText(buildReport.get().readText())
  }
}
