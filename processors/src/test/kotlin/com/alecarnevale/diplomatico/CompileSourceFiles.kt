package com.alecarnevale.diplomatico

import com.alecarnevale.diplomatico.providers.HashingRoomDBVersionProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
internal fun compileSourceFiles(vararg sourceFiles: SourceFile): KspCompilationResult {
  val kotlinCompilation =
    KotlinCompilation().apply {
      sources = sourceFiles.toMutableList().apply { add(databaseAnnotation) }
      symbolProcessorProviders = listOf(HashingRoomDBVersionProcessorProvider())
      inheritClassPath = true
    }
  return KspCompilationResult(
    sourcesDir = kotlinCompilation.kspSourcesDir,
    result = kotlinCompilation.compile(),
  )
}

// we mirror Room Database annotation just for testing purpose
// otherwise it couldn't possible access its argument
private val databaseAnnotation by lazy {
  SourceFile.kotlin(
    "Database.kt",
    """
    package androidx.room

    annotation class Database(
      val entities: Array<KClass<*>> = []
    )
    """.trimIndent(),
  )
}
