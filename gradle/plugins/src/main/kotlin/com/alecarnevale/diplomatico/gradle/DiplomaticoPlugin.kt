package com.alecarnevale.diplomatico.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

// TODO: missing tests
internal class DiplomaticoPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.dependencies.add("implementation", target.project(":api"))
    target.dependencies.add("ksp", target.project(":processors"))

    target.tasks.register("checkRoomLevels", CheckRoomLevelsTask::class.java) {
      it.group = "diplomatico"
    }
    target.tasks.register("updateRoomLevels", UpdateRoomLevelsTask::class.java) {
      it.group = "diplomatico"
    }

    // TODO support debug/release
    target.tasks.named("assemble") {
      it.finalizedBy(target.tasks.named("checkRoomLevels"))
    }
  }
}
