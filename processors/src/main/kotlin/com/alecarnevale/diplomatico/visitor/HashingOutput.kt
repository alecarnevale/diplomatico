package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * This data class to store those data that are considered as input for the hashing function.
 *
 * In the beginning the input was the entire file,
 * but such a naive solution wouldn't work when the file lives in a different module rather than one under KSP processing
 * because KSP processor cannot access a source file of a dependency module.
 * So, let's consider just its (compiled) properties instead of the entire source file.
 */
private data class HashingDataHolder(
  val qualifiedName: String,
  val properties: List<Property>,
) {
  data class Property(
    val qualifiedName: String,
    val typeQualifiedName: String,
  )

  /**
   * Compute hash function of this HashingDataHolder.
   */
  fun hash(): String =
    properties
      .flatMap { listOf(it.qualifiedName, it.typeQualifiedName) }
      .plus(qualifiedName)
      .merge()
}

internal class HashingOutput(
  private val resolver: Resolver,
  private val logger: KSPLogger,
) {
  /**
   * Returns [Output] for requested [roomDBClass], starting [contributingClasses].
   *
   * @param roomDBClass the database classes for which generate [Output.qualifiedName].
   * @param contributingClasses all classes contributing to generate [Output.hash].
   */
  fun generate(
    roomDBClass: KSClassDeclaration,
    contributingClasses: Set<KSClassDeclaration>,
  ): Output? {
    // generate hash for each file in contributingClasses
    val entitiesFilePath =
      contributingClasses.map {
        it.containingFile!!.filePath
      }
    // generate hash also for nested class in contributingClasses
    // so, retrieve file path of nested classes for each contributingClasses
    val nestedClassesFilePath =
      contributingClasses.flatMap { nestedClass ->
        nestedClass.resolveNestedClassesPath()
      }

    val hash =
      entitiesFilePath
        .map { hashingFile(it) }
        // concat entities' hashes with its nested class' hashes
        .plus(nestedClassesFilePath.map { hashingFile(it) })
        // generate a single String starting many ones
        .merge()

    val databaseQualifiedName = roomDBClass.qualifiedName?.asString()
    if (databaseQualifiedName == null) {
      logger.error("Error while getting qualifiedName for $roomDBClass")
      return null
    }

    return Output(
      hash = hash,
      qualifiedName = databaseQualifiedName,
    )
  }

  data class Output(
    val qualifiedName: String,
    val hash: String,
  )

  private fun hashingFile(filePath: String): String =
    with(MessageDigest.getInstance("SHA-256").digest(File(filePath).readBytes())) {
      Base64.getEncoder().encodeToString(this)
    }

  // extract path of other classes that is being references in this entity (or class when call recursively)
  private fun KSClassDeclaration.resolveNestedClassesPath(): List<String> =
    declarations
      .toList()
      .filterIsInstance<KSPropertyDeclaration>()
      .flatMap { property ->
        property.resolveFilePath()
      }.filterNotNull()

  // return the file path of this declaration, if it's not a primitive type
  private fun KSPropertyDeclaration.resolveFilePath(): List<String?> {
    val classDeclaration =
      type.resolve().declaration.qualifiedName?.let {
        // when resolving a primitive type (Int, String...) null is returned
        resolver.getClassDeclarationByName(it)
      }
    // return this file path (if not null) + any other file path discovered with this as root
    return with(classDeclaration) {
      this?.resolveNestedClassesPath()?.plus(this.containingFile?.filePath) ?: emptyList()
    }
  }
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
