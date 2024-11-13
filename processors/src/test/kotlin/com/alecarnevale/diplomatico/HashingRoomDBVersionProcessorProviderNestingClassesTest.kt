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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooParent::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,y9BpgmFflfSqgBI0zsxl9beoQuF6+MBJxQK9R0HMuD8=
      
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
      com.example.FooDatabase,b3OUOqDP0bKgWmkE+txWoaf2GSyr9UyyGALyub9XhBI=
      
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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooParent::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooGrandChild, fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,O76dRCipSUXmmNdI1CqzlspgjHylLro4IwMMFDD3wG8=
      
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
      com.example.FooDatabase,rOA78GDWden8Oo8pE0ROyTSJ14vEBaKiedH15fyxtrM=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class FooParent with a nested enum class FooChild and its FooDatabase, WHEN @HashingRoomDBVersion is applied to FooDatabase and a new enum value of FooChild appears, THEN hashing value in the report changes`() {
    var fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example

        enum class FooChild {
          X
        }
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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooParent::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,Iy5TO4RqN7utUl4D974I4e7tTk2N2/DZDJqYsR3/DmE=
      
      """,
    )

    fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example

        enum class FooChild {
          X, Y
        }
        """.trimIndent(),
      )

    result = compileSourceFiles(fooChild, fooParent, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.FooDatabase,3FrIfvlCGL9vamIz7ZU2yXkPa3SK5sZY2mfR62s6shk=
      
      """,
    )
  }
}
