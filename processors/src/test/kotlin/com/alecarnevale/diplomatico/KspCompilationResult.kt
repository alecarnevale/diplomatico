package com.alecarnevale.diplomatico

import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

@OptIn(ExperimentalCompilerApi::class)
internal data class KspCompilationResult(
  private val sourcesDir: File,
  val result: KotlinCompilation.Result,
) {
  val generatedSources: List<File> get() = sourcesDir.listFilesRecursively()

  private fun File.listFilesRecursively(): List<File> =
    listFiles().orEmpty().flatMap { file ->
      if (file.isDirectory) file.listFilesRecursively() else listOf(file)
    }
}

// because file is store in a tmp folder like /var/folders/.../ksp/sources/resources/
private val File.kspSourcePath: String get() = path.substringAfter("/ksp/sources/resources/")

internal fun KspCompilationResult.assertZeroGeneratedResources() = assertGeneratedResources()

internal fun KspCompilationResult.assertGeneratedResources(vararg generatedSources: String) {
  val sourcesPaths = this.generatedSources.map { file -> file.kspSourcePath }
  assertEquals(generatedSources.toSet(), sourcesPaths.toSet())
}

internal fun KspCompilationResult.assertGeneratedContent(
  sourcePath: String,
  content: String,
) {
  val files = generatedSources.filter { file -> file.kspSourcePath == sourcePath }
  return when (files.size) {
    0 -> throw AssertionError("No files found for path $sourcePath.")
    1 -> assertEquals(content.trimIndent(), files.first().readText())
    else -> throw AssertionError("Multiple files found for path $sourcePath.")
  }
}
