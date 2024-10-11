package com.alecarnevale.diplomatico

import com.alecarnevale.diplomatico.providers.HashingRoomDBVersionProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCompilerApi::class)
class HashingRoomDBVersionProcessorProviderTest {
  @Test
  fun `GIVEN a class Foo without Database annotation WHEN @HashingRoomDBVersion is applied to Foo, THEN compilation error and no report is not generated`() {
    val foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        class Foo
        """.trimIndent(),
      )

    val result = compileSourceFiles(foo)

    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.result.exitCode)

    result.assertZeroGeneratedResources()
  }

  @Test
  fun `GIVEN a class FooDatabase with Database annotation but no entities field WHEN @HashingRoomDBVersion is applied to Foo, THEN compilation error and no report is not generated`() {
    val fooDatabase =
      SourceFile.kotlin(
        "FooDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database
        class FooDatabase
        """.trimIndent(),
      )

    val result = compileSourceFiles(fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.result.exitCode)

    result.assertZeroGeneratedResources()
  }

  @Test
  fun `GIVEN a class Foo with Database annotation but empty entities field WHEN @HashingRoomDBVersion is applied to Foo, THEN compilation error and no report is not generated`() {
    val fooDatabase =
      SourceFile.kotlin(
        "FooDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [])
        class FooDatabase
        """.trimIndent(),
      )

    val result = compileSourceFiles(fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.result.exitCode)

    result.assertZeroGeneratedResources()
  }

  @Test
  fun `GIVEN a class Foo with Database annotation and entities field WHEN @HashingRoomDBVersion is applied to Foo, THEN a report is generated`() {
    val fooEntity =
      SourceFile.kotlin(
        "FooEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooEntity(
          val x: Int,
        )
        """.trimIndent(),
      )

    val fooDatabase =
      SourceFile.kotlin(
        "FooDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooEntity::class])
        class FooDatabase
        """.trimIndent(),
      )

    val result = compileSourceFiles(fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,rH6hy7aLdVv1fxCZlPqtsqRZauGUsJTim0CxRMDo8vg=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class Foo and a class Bar with Database annotation and entities field WHEN @HashingRoomDBVersion is applied to Foo and Bar, THEN a report is generated with 2 entry`() {
    val fooEntity =
      SourceFile.kotlin(
        "FooEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooEntity(
          val x: Int,
        )
        """.trimIndent(),
      )

    val fooDatabase =
      SourceFile.kotlin(
        "FooDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooEntity::class])
        class FooDatabase
        """.trimIndent(),
      )

    val barEntity1 =
      SourceFile.kotlin(
        "BarEntity1.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class BarEntity1(
          val x: Int,
        )
        """.trimIndent(),
      )

    val barEntity2 =
      SourceFile.kotlin(
        "BarEntity2.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class BarEntity2(
          val x: Int,
        )
        """.trimIndent(),
      )

    val barDatabase =
      SourceFile.kotlin(
        "BarDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [BarEntity1::class, BarEntity2::class])
        class BarDatabase
        """.trimIndent(),
      )

    val result = compileSourceFiles(fooEntity, fooDatabase, barEntity1, barEntity2, barDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,OjhLbkbaR7D2Z5TUDoLt7Llsd/terCdAi1jI3pdMbuU=
      com.example.FooDatabase,rH6hy7aLdVv1fxCZlPqtsqRZauGUsJTim0CxRMDo8vg=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class Foo with Database annotation and entities field WHEN @HashingRoomDBVersion is applied to Foo but Foo changes, THEN reports generated change`() {
    var fooEntity =
      SourceFile.kotlin(
        "FooEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooEntity(
          val x: Int,
        )
        """.trimIndent(),
      )

    val fooDatabase =
      SourceFile.kotlin(
        "FooDatabase.kt",
        """
        package com.example

        import androidx.room.Database
        import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooEntity::class])
        class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,rH6hy7aLdVv1fxCZlPqtsqRZauGUsJTim0CxRMDo8vg=
      
      """,
    )

    fooEntity =
      SourceFile.kotlin(
        "FooEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooEntity(
          val x: Int,
          val y: String,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,NyNDpYWpN9vFEOcaDrAI7JLXXA6MgaIKN1TLQQ7m/ms=
      
      """,
    )
  }

  private fun compileSourceFiles(vararg sourceFiles: SourceFile): KspCompilationResult {
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
