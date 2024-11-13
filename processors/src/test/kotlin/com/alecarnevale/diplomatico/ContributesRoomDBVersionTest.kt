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
      com.example.BarDatabase,njAp7m9Tsys6zpPSDrIm9nqwpZUInkgEMeEvmmC/EJM=
      
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
      com.example.BarDatabase,VwN0MfjF1NpvTHZVXmyn2oUuRFrOywTlFLR7oAOWIf4=
      
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
      com.example.BarDatabase,rEiuH2b2S1D3CSTkpNqiotC1Ni8YaRt3c54nEGzHPiU=
      
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
      com.example.BarDatabase,jprjjxUu6hflojmiMjLaXISJZroe2yHY8S5knK7ivlw=
      
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
      com.example.BarDatabase,PLwiV1H4Ff/Gs4G5aR3TE3r/n3SNuvezaAtltRFq874=
      
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
        com.example.BarDatabase,0TT0oeFKYYa3LVMDU5Iw8cQq+mn7R5eu+WN2Ue/koIk=
        com.example.FooDatabase,xv3SgBTwWu3J6QghkxooWXgDblHhDtyK6azPwAttli0=
      
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
        com.example.BarDatabase,0TT0oeFKYYa3LVMDU5Iw8cQq+mn7R5eu+WN2Ue/koIk=
        com.example.FooDatabase,+JwoZOKawATYKV3R/Vd2lJroFoRq8aVgoYcbwKlpC5M=
      
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
        com.example.BarDatabase,4ZWwXLBe4200CGcpxHUOj/cC5UF3VqN9ypiwB4PnWkQ=
        com.example.FooDatabase,+JwoZOKawATYKV3R/Vd2lJroFoRq8aVgoYcbwKlpC5M=
      
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
        com.example.FooDatabase,pEcTXkEPhpDI8XPOWiVGUoNVpOKoDOOtNS1eRTr6f3M=
      
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
        com.example.FooDatabase,bARZ4JEOwtZb8oDUsVSqAvTVNKl6HUOFig+4fhK1Mvw=
      
      """,
    )
  }
}
