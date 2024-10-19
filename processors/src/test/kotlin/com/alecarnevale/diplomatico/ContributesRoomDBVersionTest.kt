package com.alecarnevale.diplomatico

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// Test suite to cover usage of ContributesRoomDBVersion annotation
@OptIn(ExperimentalCompilerApi::class)
internal class ContributesRoomDBVersionTest {
  @Test
  fun `GIVEN a class Foo, a database Bar and an Entity BarEntity for that database, WHEN @ContributesRoomDBVersion is applied to Foo, THEN any changes to Foo changes also the hashing value in the report`() {
    // generate a report without any class annotated with @ContributesRoomDBVersion
    val barEntity =
      SourceFile.kotlin(
        "BarEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class BarEntity(
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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [BarEntity::class])
        abstract class BarDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(barEntity, barDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,ZvATGjEhqm3UGo4VWWDbbInWN8xLDlwYcSsn0i9oGFE=
      
      """,
    )

    // generate a report with Foo annotated with @ContributesRoomDBVersion
    // Foo isn't required to referenced by BarEntity
    var foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
        
        @ContributesRoomDBVersion(roomDB = BarDatabase::class)
        data class Foo(
          val x: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(barEntity, barDatabase, foo)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,Ca1ipokz9aM1frZdoZvny2JBnXG/uupLU/L/guJcacM=
      
      """,
    )

    // trigger a difference for Foo to generate a change in the report
    foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
        
        @ContributesRoomDBVersion(roomDB = BarDatabase::class)
        data class Foo(
          val x: Int,
          val y: String,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(barEntity, barDatabase, foo)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,7rxoYLX1cg4rXv8bt72ACyjnPDNd0vQbtQiyomVXyjQ=
      
      """,
    )
  }

  @Test
  fun `GIVEN a class Foo, a database Bar and an Entity BarEntity for that database, WHEN @ContributesRoomDBVersion is applied to Foo, THEN any changes to a nested class of Foo changes also the hashing value in the report`() {
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

    // Foo isn't required to referenced by BarEntity
    val foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
        
        @ContributesRoomDBVersion(roomDB = BarDatabase::class)
        data class Foo(
          val x: FooChild,
        )
        """.trimIndent(),
      )

    val barEntity =
      SourceFile.kotlin(
        "BarEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class BarEntity(
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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [BarEntity::class])
        abstract class BarDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(barEntity, barDatabase, foo, fooChild)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,UNc/khXpR0vSnCzefOVV3x0RKOcNgsm4uOb+0AtcWko=
      
      """,
    )

    // trigger a difference for a nested class of Foo to generate a change in the report
    fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example
        
        data class FooChild(
          val x: Int,
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(barEntity, barDatabase, foo, fooChild)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
      com.example.BarDatabase,i1HNIf2IKRAQq82z3KZSSwoDoBpM/ZekEee+4ZGET54=
      
      """,
    )
  }

  @Test
  fun `GIVEN multiple class for different databases, WHEN they're annotated with @ContributesRoomDBVersion, THEN only record for the database which class has changed get changes too`() {
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

    val foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
        
        @ContributesRoomDBVersion(roomDB = FooDatabase::class)
        data class Foo(
          val x: FooChild,
        )
        """.trimIndent(),
      )

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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [FooEntity::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var barChild =
      SourceFile.kotlin(
        "BarChild.kt",
        """
        package com.example
        
        data class BarChild(
          val x: Int,
        )
        """.trimIndent(),
      )

    val bar =
      SourceFile.kotlin(
        "Bar.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
        
        @ContributesRoomDBVersion(roomDB = BarDatabase::class)
        data class Bar(
          val x: BarChild,
        )
        """.trimIndent(),
      )

    val barEntity =
      SourceFile.kotlin(
        "BarEntity.kt",
        """
        package com.example

        import androidx.room.Entity
        
        @Entity
        data class BarEntity(
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
        import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
        
        @HashingRoomDBVersion
        @Database(entities = [BarEntity::class])
        abstract class BarDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(fooChild, foo, fooEntity, fooDatabase, barChild, bar, barEntity, barDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        com.example.BarDatabase,N4w+9bOiBO5H8lnV1N/lNydU4Ea7JLukzX7hybPnszQ=
        com.example.FooDatabase,cI5RWIS4pdCkRqBFDrsoF3QSAfbRxYjr3N1ss0Xamdo=
      
      """,
    )

    // trigger a difference only for foo
    fooChild =
      SourceFile.kotlin(
        "FooChild.kt",
        """
        package com.example
        
        data class FooChild(
          val x: Int,
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(fooChild, foo, fooEntity, fooDatabase, barChild, bar, barEntity, barDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        com.example.BarDatabase,N4w+9bOiBO5H8lnV1N/lNydU4Ea7JLukzX7hybPnszQ=
        com.example.FooDatabase,1RH+e+UF9smlGSoyPtBoQMdKGRomgw/Anjt598me14Q=
      
      """,
    )

    // trigger a difference only for bar
    barChild =
      SourceFile.kotlin(
        "BarChild.kt",
        """
        package com.example
        
        data class BarChild(
          val x: Int,
          val y: Int,
        )
        """.trimIndent(),
      )

    result = compileSourceFiles(fooChild, foo, fooEntity, fooDatabase, barChild, bar, barEntity, barDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        com.example.BarDatabase,1tqYFpBmDzQmb1mU+2Z9CxmzTTy7UUCwhFphfBBbK/k=
        com.example.FooDatabase,1RH+e+UF9smlGSoyPtBoQMdKGRomgw/Anjt598me14Q=
      
      """,
    )
  }

  @Test
  fun `GIVEN a FooEntity for a database FooDatabase annotated with @HashingRoomDBVersion and an plain class Foo annotated with @ContributesRoomDBVersion, WHEN a change for Foo appears, THEN the report is updated even if FooEntity didn't change`() {
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

    val foo =
      SourceFile.kotlin(
        "Foo.kt",
        """
        package com.example

        import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion

        @ContributesRoomDBVersion(roomDB = FooDatabase::class)
        data class Foo(
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
        
        @HashingRoomDBVersion
        @Database(entities = [FooEntity::class])
        abstract class FooDatabase
        """.trimIndent(),
      )

    var result = compileSourceFiles(foo, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        com.example.FooDatabase,WPc7hJzl2lwuRhvC+6tN6nqrlg91zqwuhfqHsbNn86c=
      
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

    result = compileSourceFiles(foo, fooEntity, fooDatabase)

    assertEquals(KotlinCompilation.ExitCode.OK, result.result.exitCode)

    result.assertGeneratedResources("com/alecarnevale/diplomatico/results/report.csv")
    result.assertGeneratedContent(
      "com/alecarnevale/diplomatico/results/report.csv",
      """
        com.example.FooDatabase,5FX3v/br+vOTRegzl/BZ9QgpSn3psLn40T5QZ7fjC5I=
      
      """,
    )
  }
}
