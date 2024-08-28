package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Returns file content hash and fully qualified name of the specified class.
 */
internal class AutoIncrementRoomDBVersionVisitor(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) : KSEmptyVisitor<Unit, AutoIncrementRoomDBVersionVisitor.Output?>() {
  override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit,
  ): Output? {
    val roomDatabaseDeclaration =
      classDeclaration.annotations.firstOrNull {
        it.shortName.asString() == "Database"
      } ?: run {
        logger.error("Missing Database annotation for ${classDeclaration.qualifiedName?.asString()}")
        return null
      }
    val entitiesKSType = roomDatabaseDeclaration.arguments.firstOrNull { it.name?.asString() == "entities" }?.value as? List<KSType>
    if (entitiesKSType.isNullOrEmpty()) {
      logger.error("No Entity defined for the Database ${classDeclaration.qualifiedName?.asString()}")
      return null
    }

    val entitiesClassDeclaration: List<KSClassDeclaration> =
      entitiesKSType.mapNotNull { entityKSType ->
        entityKSType.declaration.qualifiedName?.let { entityKsName ->
          resolver.getClassDeclarationByName(entityKsName)
        }
      }

    val entitiesFilePath =
      entitiesClassDeclaration.map {
        it.containingFile!!.filePath
      }
    val hash = entitiesFilePath.map { hashingFile(it) }.merge()

    val qualifiedName = classDeclaration.qualifiedName?.asString()
    if (qualifiedName == null) {
      logger.error("Error while get qualifiedName for $classDeclaration")
      return null
    }

    return Output(
      hash = hash,
      qualifiedName = qualifiedName,
    )
  }

  data class Output(
    val hash: String,
    val qualifiedName: String,
  )

  override fun defaultHandler(
    node: KSNode,
    data: Unit,
  ): Output? = null

  private fun hashingFile(filePath: String): String =
    with(MessageDigest.getInstance("SHA-256").digest(File(filePath).readBytes())) {
      Base64.getEncoder().encodeToString(this)
    }

  private fun List<String>.merge(): String {
    val hashesByteArray: ByteArray =
      fold(byteArrayOf()) { acc: ByteArray, elem: String ->
        acc + elem.toByteArray()
      }
    return with(MessageDigest.getInstance("SHA-256").digest(hashesByteArray)) {
      Base64.getEncoder().encodeToString(this)
    }
  }
}
