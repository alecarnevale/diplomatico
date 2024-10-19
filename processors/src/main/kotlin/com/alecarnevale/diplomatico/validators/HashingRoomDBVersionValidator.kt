package com.alecarnevale.diplomatico.validators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

private const val ERROR_MESSAGE = "HashingRoomDBVersion annotation must annotates an abstract class extending androidx.room.RoomDatabase."

internal class HashingRoomDBVersionValidator(
  private val logger: KSPLogger,
) {
  /**
   * Check annotated symbol is an abstract class extending at least one supertype
   * (should be androidx.room.RoomDatabase, but not checking to speed up computation).
   * Returns annotated symbol casted as KSClassDeclaration.
   *
   * @param annotatedDatabase symbol annotated with [HashingRoomDBVersion].
   * @return annotatedDatabase casted as KSClassDeclaration, if it meets all criteria. Null otherwise.
   */
  fun validate(annotatedDatabase: KSAnnotated): KSClassDeclaration? {
    val classDeclaration = annotatedDatabase.toClassDeclaration() ?: return null
    if (!classDeclaration.isAbastractClass() || !classDeclaration.extendsOrImplementsRoomDatabase()) {
      return null
    }
    return classDeclaration
  }

  private fun KSAnnotated.toClassDeclaration(): KSClassDeclaration? {
    val classDeclaration = (this as? KSClassDeclaration)
    if (classDeclaration == null) {
      logger.error(ERROR_MESSAGE)
    }
    return classDeclaration
  }

  private fun KSClassDeclaration.isAbastractClass(): Boolean {
    if (this.classKind != ClassKind.CLASS) {
      logger.error(ERROR_MESSAGE)
      return false
    }
    if (!this.modifiers.contains(Modifier.ABSTRACT)) {
      logger.error(ERROR_MESSAGE)
      return false
    }

    return true
  }

  private fun KSClassDeclaration.extendsOrImplementsRoomDatabase(): Boolean =
    when (superTypes.count()) {
      0 ->
        false.also {
          logger.error(ERROR_MESSAGE)
        }

      else -> true
    }
}
