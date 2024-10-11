package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Returns file content hash and fully qualified name of the specified class.
 */
internal class HashingRoomDBVersionVisitor(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) : KSEmptyVisitor<Unit, HashingRoomDBVersionVisitor.Output?>() {
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
    // retrieve file path of nested classes for each entity
    val nestedClassesFilePath =
      entitiesClassDeclaration.flatMap { entity ->
        entity.resolveNestedEntitiesPath()
      }

    val hash =
      entitiesFilePath
        .map { hashingFile(it) }
        // contact entities hashes with its nested class hashes
        .plus(nestedClassesFilePath.map { hashingFile(it) })
        // generate a single String from many ones
        .merge()

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
    val qualifiedName: String,
    val hash: String,
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

  // extract path of other classes that is being references in this entity
  private fun KSClassDeclaration.resolveNestedEntitiesPath(): List<String> =
    declarations
      .toList()
      .filterIsInstance<KSPropertyDeclaration>()
      .flatMap { property ->
        property.resolveFilePath()
      }.filterNotNull()

  // return the file path of this declaration, if it's not a built-in type
  private fun KSPropertyDeclaration.resolveFilePath(): List<String?> {
    val classDeclaration =
      type.resolve().declaration.qualifiedName?.let {
        resolver.getClassDeclarationByName(it)
      }
    // return this file path (if not null) + any other file path discovered with this as root
    return with(classDeclaration) {
      this?.resolveNestedEntitiesPath()?.plus(this.containingFile?.filePath) ?: emptyList()
    }
  }
}
