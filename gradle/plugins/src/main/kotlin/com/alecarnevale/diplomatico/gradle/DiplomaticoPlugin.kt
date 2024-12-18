package com.alecarnevale.diplomatico.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

// TODO: missing tests
internal class DiplomaticoPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.dependencies.add("implementation", target.project(":annotations"))
    target.dependencies.add("ksp", target.project(":processors"))

    with(target.extensions.getByType(AndroidComponentsExtension::class.java)) {
      onVariants {
        val buildVariant =
          if (it.flavorName.isNullOrBlank()) {
            it.buildType!!
          } else {
            "${it.flavorName}${it.buildType?.capitalized()}"
          }
        target.setupCheckRoomVersionsTask(buildVariant)
        target.setupUpdateRoomVersionsTask(buildVariant)
      }
    }
  }

  private fun Project.setupCheckRoomVersionsTask(buildType: String) {
    val taskName = "checkRoomVersions${buildType.capitalized()}"
    tasks.register(taskName, CheckRoomVersionsTask::class.java) {
      it.group = "diplomatico"
    }
    tasks.named(taskName, CheckRoomVersionsTask::class.java) {
      it.buildReport.convention(
        project.layout.buildDirectory
          .file("generated/ksp/$buildType/resources/com/alecarnevale/diplomatico/results/report.csv")
          .get()
          .asFile,
      )
      // TODO: path of report file tracked with git could be optionally set in input
      val assetFile =
        project.layout.projectDirectory
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

    tasks
      .matching {
        // in a multimodule project, the assemble of :app module doesn't request the assemble of other subprojects which it depends on
        // but it always requires to run ksp on those modules
        it.name.startsWith("ksp") && it.name.contains(buildType, ignoreCase = true)
      }.all {
        it.finalizedBy(tasks.named(taskName))
      }
  }

  private fun Project.setupUpdateRoomVersionsTask(buildType: String) {
    val taskName = "updateRoomVersions${buildType.capitalized()}"
    tasks.register(taskName, UpdateRoomVersionsTask::class.java) {
      it.group = "diplomatico"
    }
    tasks.named(taskName, UpdateRoomVersionsTask::class.java) {
      it.buildReport.convention(
        project.layout.buildDirectory
          .file("generated/ksp/$buildType/resources/com/alecarnevale/diplomatico/results/report.csv")
          .get()
          .asFile,
      )
      // TODO: path of report file tracked with git could be optionally set in input
      val assetFile =
        project.layout.projectDirectory
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

    tasks
      .named(taskName)
      .get()
      .dependsOn(
        tasks.matching { it.name.startsWith("ksp") },
      )
  }
}
