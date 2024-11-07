package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
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
  val qualifiedName: String?,
  val properties: List<Property>,
) {
  data class Property(
    val qualifiedName: String?,
    val typeQualifiedName: KSType,
  )

  /**
   * Compute hash function of this HashingDataHolder.
   */
  fun hash(): String =
    properties
      .flatMap {
        listOf(
          it.qualifiedName,
          it.typeQualifiedName.declaration.qualifiedName
            ?.asString(),
        )
      }.plus(qualifiedName)
      .filterNotNull()
      .merge()
}

internal class HashingOutput(
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
    val entitiesHashingDataHolders: List<HashingDataHolder> =
      contributingClasses.map {
        it.toHashingDataHolder()
      }
    // generate hash also for nested properties of entities
    // so, retrieve data holders of nested properties for each entitiesHashingDataHolders (recursively)
    val nestedHashingDataHolders: List<HashingDataHolder> =
      entitiesHashingDataHolders.flatMap { entityHashingDataHolder ->
        entityHashingDataHolder.resolveNestedProperties()
      }

    val hash =
      entitiesHashingDataHolders
        .map { it.hash() }
        // concat entities' hashes with its nested class' hashes
        .plus(nestedHashingDataHolders.map { it.hash() })
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

  private fun KSClassDeclaration.toHashingDataHolder(): HashingDataHolder =
    HashingDataHolder(
      qualifiedName = qualifiedName?.asString(),
      properties =
        getAllProperties().toList().map { property ->
          HashingDataHolder.Property(
            qualifiedName = property.qualifiedName?.asString(),
            typeQualifiedName = property.type.resolve(),
          )
        },
    )

  // extract properties of other classes that is being references in this entity
  private fun HashingDataHolder.resolveNestedProperties(): List<HashingDataHolder> =
    properties
      .mapNotNull { property ->
        val classProperty = property.typeQualifiedName.declaration as? KSClassDeclaration
        classProperty?.toHashingDataHolder()
      }.run {
        // recursively append HashingDataHolder until no more property to process in the tree
        this.plus(this.flatMap { it.resolveNestedProperties() })
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
