package com.alecarnevale.diplomatico.visitor

import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.visitor.KSEmptyVisitor

/**
 * Returns the database class declaration defined by applying [ContributesRoomDBVersion] with the [ContributesRoomDBVersion.roomDB] specified.
 */
internal class ContributesRoomDBVersionVisitor(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) : KSEmptyVisitor<Unit, KSClassDeclaration?>() {
  override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit,
  ): KSClassDeclaration? =
    // find ContributesRoomDBVersion annotation
    classDeclaration.annotations.firstOrNull { it.shortName.getShortName() == ContributesRoomDBVersion::class.simpleName }?.let { annotation ->
      // find ContributesRoomDBVersion.roomDB argument
      (annotation.arguments.firstOrNull { it.name?.getShortName() == ContributesRoomDBVersion::roomDB.name }?.value as? KSType)?.let { argument ->
        // find KSClassDeclaration for the specified argument value
        argument.declaration.qualifiedName?.let {
          resolver.getClassDeclarationByName(it)
        } ?: run {
          logger.error("No ContributesRoomDBVersion::roomDB found while visiting $classDeclaration")
          null
        }
      }
    }

  override fun defaultHandler(
    node: KSNode,
    data: Unit,
  ): KSClassDeclaration? = null
}
