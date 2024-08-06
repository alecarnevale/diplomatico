package com.alecarnevale.diplomatico.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * This task update the report file in the asset folder with entries contained in the build report.
 */
internal abstract class UpdateRoomLevelsTask : DefaultTask() {
  @TaskAction
  fun check() {
    TODO("To do")
  }
}
