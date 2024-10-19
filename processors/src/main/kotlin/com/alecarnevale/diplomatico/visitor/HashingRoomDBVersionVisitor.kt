package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.visitor.KSEmptyVisitor

/**
 * Returns entities class declaration defined for the Room DB targeted with [HashingRoomDBVersion] annotation.
 */
internal class HashingRoomDBVersionVisitor(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) : KSEmptyVisitor<Unit, Set<KSClassDeclaration>?>() {
  override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit,
  ): Set<KSClassDeclaration>? {
    // start by finding Room Database annotation
    val roomDatabaseDeclaration =
      classDeclaration.annotations.firstOrNull {
        it.shortName.asString() == "Database"
      } ?: run {
        logger.error("Missing Database annotation for ${classDeclaration.qualifiedName?.asString()}")
        return null
      }
    // then extracting each entity defined in the Room Database annotation
    val entitiesKSType = roomDatabaseDeclaration.arguments.firstOrNull { it.name?.asString() == "entities" }?.value as? List<KSType>
    if (entitiesKSType.isNullOrEmpty()) {
      logger.error("No Entity defined for the Database ${classDeclaration.qualifiedName?.asString()}")
      return null
    }

    return entitiesKSType
      .mapNotNull { entityKSType ->
        entityKSType.declaration.qualifiedName?.let { entityKsName ->
          resolver.getClassDeclarationByName(entityKsName)
        }
      }.toSet()
  }

  override fun defaultHandler(
    node: KSNode,
    data: Unit,
  ): Set<KSClassDeclaration>? = null
}
