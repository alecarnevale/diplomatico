package com.alecarnevale.diplomatico

import com.alecarnevale.diplomatico.providers.AutoIncrementRoomDBVersionProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class AutoIncrementRoomDBVersionProcessorProviderTest {
  @Test
  fun `WHEN @AutoBinds is applied to a val, THEN compilation error and hilt module is not generated`() {
    val src =
      SourceFile.kotlin(
        "Foo.kt",
        """
      package com.example

      import com.alecarnevale.diplomatico.api.AutoIncrementRoomDBVersion
      
      @AutoIncrementRoomDBVersion
      data class Foo(
        val x: Int
      )
      """,
      )
    println("TEST_ALE: $src")

    val result = compileSourceFiles(src)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        m+g8Ckp989+D+7jPmZzYNkocoAyGLkUqV5d2LZTO0FQ=,com.example.Foo
      """,
    )
  }

  private fun compileSourceFiles(vararg sourceFiles: SourceFile): KspCompilationResult {
    val kotlinCompilation =
      KotlinCompilation().apply {
        sources = sourceFiles.toList()
        symbolProcessorProviders = listOf(AutoIncrementRoomDBVersionProcessorProvider())
        inheritClassPath = true
      }
    return KspCompilationResult(
      sourcesDir = kotlinCompilation.kspSourcesDir,
      result = kotlinCompilation.compile(),
    )
  }
}
