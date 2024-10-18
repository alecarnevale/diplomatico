package com.alecarnevale.diplomatico.validators

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

private const val ERROR_MESSAGE = "ContributesRoomDBVersion annotation must annotates a class."

internal class ContributesRoomDBVersionValidator(
  private val logger: KSPLogger,
) {
  /**
   * Check annotated symbol is a class.
   * Returns annotated symbol casted as KSClassDeclaration.
   *
   * @param annotatedEntity symbol annotated with [ContributesRoomDBVersion].
   * @return annotatedDatabase casted as KSClassDeclaration, if it meets all criteria. Null otherwise.
   */
  fun validate(annotatedEntity: KSAnnotated): KSClassDeclaration? = annotatedEntity.toClassDeclaration()

  private fun KSAnnotated.toClassDeclaration(): KSClassDeclaration? {
    val classDeclaration = (this as? KSClassDeclaration)
    if (classDeclaration == null) {
      logger.error(ERROR_MESSAGE)
    }
    return classDeclaration
  }
}
