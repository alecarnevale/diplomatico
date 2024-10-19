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
      com.example.BarDatabase,PbsZleT17v4mrPd1sMuKSvMKwu8Dj4J8iwxd8wBswFU=
      
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
      com.example.BarDatabase,yZQQbCjuGB293KdFU+7/ZJ6fRfkzzcF9+/ZDVz0dTU0=
      
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
      com.example.BarDatabase,kjddYyYByfhBr7R85oeN70508A9ymp939ZacOxGpYkg=
      
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
      com.example.BarDatabase,GF1J+oaU5xxRm6nwkYRqLkUegry2vlF1DDcR3rdl/JQ=
      
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
        com.example.BarDatabase,xLiHL73pNM4H5nEFXj91VFYddZXL0ZT63xN3jrWQdPE=
        com.example.FooDatabase,UjGzenA9bmXBUajTnFXhwAXaIgYHfVoYtDGHg69EXoA=
      
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
        com.example.BarDatabase,xLiHL73pNM4H5nEFXj91VFYddZXL0ZT63xN3jrWQdPE=
        com.example.FooDatabase,OdbVFva8iHMuyKSCzcGlciYGsQrWy3WqDTfYdPPGQ6o=
      
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
        com.example.BarDatabase,7QN+Vju1AjqDF5CihalezTin3T/PfxfaoVd9H4x1a5U=
        com.example.FooDatabase,OdbVFva8iHMuyKSCzcGlciYGsQrWy3WqDTfYdPPGQ6o=
      
      """,
    )
  }
}
