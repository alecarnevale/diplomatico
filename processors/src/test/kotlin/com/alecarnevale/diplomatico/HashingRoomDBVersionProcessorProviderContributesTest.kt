package com.alecarnevale.diplomatico

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// Test suite to cover only contributes argument cases
@OptIn(ExperimentalCompilerApi::class)
internal class HashingRoomDBVersionProcessorProviderContributesTest {
  @Test
  fun `GIVEN a class Bar a class FooEntity and its FooDatabase, WHEN @HashingRoomDBVersion is applied to FooDatabase but Bar is not a contributes argument, THEN only changes to FooEntity change the report`() {
    var bar =
      SourceFile.kotlin(
        "Bar.kt",
        """
        package com.example

        data class Bar(
          val x: Int,
        )
        """.trimIndent(),
      )

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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion(contributes = [])
        @Database(entities = [FooEntity::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,4BIslonxrYg+Eq1OHnIu5VMwqnTOkUzD5c4j5h/Vzjw=
      
      """,
    )

    bar =
      SourceFile.kotlin(
        "Bar.kt",
        """
        package com.example

        data class Bar(
          val x: Int,
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,4BIslonxrYg+Eq1OHnIu5VMwqnTOkUzD5c4j5h/Vzjw=
      
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
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,7v4UQwIkVYI6HSAPHYNwK0Ei14R1FXKv7sEICJkpHjg=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class Bar a class FooEntity and its FooDatabase, WHEN @HashingRoomDBVersion is applied to FooDatabase with Bar as contributes argument, THEN any changes to Bar or FooEntity change the report`() {
    var bar =
      SourceFile.kotlin(
        "Bar.kt",
        """
        package com.example

        data class Bar(
          val x: Int,
        )
        """.trimIndent(),
      )

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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion(contributes = [Bar::class])
        @Database(entities = [FooEntity::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,aeI67h68n70cRgInWx1k8AKhaiOTT2Efe16F5qsi0vY=
      
      """,
    )

    bar =
      SourceFile.kotlin(
        "Bar.kt",
        """
        package com.example

        data class Bar(
          val x: Int,
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,lqzdiZY0UJp9dqHq/X0S3H69X7YlJ9dpuUGjKjjQGxg=
      
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
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(bar, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,qElCQIXAOMix6rFX+z0rY1S3GPpJVEfEIhV1z1eKLRU=
      
      """,
    )
  }
}
