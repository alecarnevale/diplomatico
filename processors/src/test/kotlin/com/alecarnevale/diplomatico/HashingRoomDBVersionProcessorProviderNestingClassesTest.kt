package com.alecarnevale.diplomatico

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// Test suite to cover only nesting classes cases
@OptIn(ExperimentalCompilerApi::class)
internal class HashingRoomDBVersionProcessorProviderNestingClassesTest {
  @Test
  fun `GIVEN a class FooParent with a nested data class FooChild and its FooDatabase, WHEN @HashingRoomDBVersion is applied to FooDatabase and a property of FooChild changes, THEN hashing value in the report changes`() {
    var fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example

        data class FooChild(
          val x: Int,
        )
        """.trimIndent(),
      )

    val fooParent =
      SourceFile.kotlin(
        "FooParent.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooParent(
          val x: FooChild,
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
        @Database(entities = [FooParent::class])
        class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,VXV8r9+A5Dah0CFH58mqUmgOPWN1GDrCeTNcglifBEs=
      
      """,
    )

    fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example

        data class FooChild(
          val x: Int,
          val y: String,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,H5kWB5LSqBIklkIObUliZiz8xff0dU9ZG/ZZcIi05tU=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class FooParent with a nested data class FooChild with a nested data class FooGrandChild and its FooDatabase, WHEN @HashingRoomDBVersion is applied to FooDatabase and a property of FooGrandChild changes, THEN hashing value in the report changes`() {
    var fooGrandChild =
      SourceFile.kotlin(
        "FooGrandChild.kt",
        """
        package com.example

        data class FooGrandChild(
          val x: Int,
        )
        """.trimIndent(),
      )

    val fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example

        data class FooChild(
          val x: FooGrandChild,
        )
        """.trimIndent(),
      )

    val fooParent =
      SourceFile.kotlin(
        "FooParent.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class FooParent(
          val x: FooChild,
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
        @Database(entities = [FooParent::class])
        class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooGrandChild, fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,OpPxRPOJUxXQenwzaahYoD5a/3i0vpMuhe+Vw+jdnSg=
      
      """,
    )

    fooGrandChild =
      SourceFile.kotlin(
        "FooGrandChild.kt",
        """
        package com.example

        data class FooGrandChild(
          val x: Int,
          val y: String,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(fooGrandChild, fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,gaomIsPFM0dfF8S13Y0V7l0EY82qiULQvlG/nJqxgLY=
      
      """,
    )
  }
}
