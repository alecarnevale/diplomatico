package com.alecarnevale.diplomatico.processors

import com.alecarnevale.diplomatico.annotations.ContributesRoomDBVersion
import com.alecarnevale.diplomatico.annotations.HashingRoomDBVersion
import com.alecarnevale.diplomatico.validators.ContributesRoomDBVersionValidator
import com.alecarnevale.diplomatico.validators.HashingRoomDBVersionValidator
import com.alecarnevale.diplomatico.visitor.ContributesRoomDBVersionVisitor
import com.alecarnevale.diplomatico.visitor.HashingOutput
import com.alecarnevale.diplomatico.visitor.HashingRoomDBVersionVisitor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Process each [HashingRoomDBVersion] to generate a report.
 */
internal class HashingRoomDBVersionProcessor(
  private val logger: KSPLogger,
  private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
  private var outputs = mutableListOf<HashingOutput.Output>()
  private var databaseResolvedClasses = setOf<KSClassDeclaration>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val databasesVisitor = HashingRoomDBVersionVisitor(resolver = resolver, logger = logger)
    val contributesVisitor = ContributesRoomDBVersionVisitor(resolver = resolver, logger = logger)
    val databaseSymbolsValidator = HashingRoomDBVersionValidator(logger)
    val contributesSymbolsValidator = ContributesRoomDBVersionValidator(logger)

    val annotationNameForDatabase = HashingRoomDBVersion::class.qualifiedName ?: return emptyList()

    val databaseResolvedSymbols = resolver.getSymbolsWithAnnotation(annotationNameForDatabase).toSet()
    if (databaseResolvedSymbols.isEmpty()) {
      return emptyList()
    }
    databaseResolvedClasses = databaseResolvedSymbols.mapNotNull { databaseSymbolsValidator.validate(it) }.toSet()

    val annotationNameForEntities = ContributesRoomDBVersion::class.qualifiedName ?: return emptyList()

    // gather all entities that are contributing for a database
    val contributesEntitiesSymbols = resolver.getSymbolsWithAnnotation(annotationNameForEntities)
    val contributesEntitiesClasses = contributesEntitiesSymbols.mapNotNull { contributesSymbolsValidator.validate(it) }
    val contributesEntitiesForDatabase: Map<KSClassDeclaration, Set<KSClassDeclaration>> =
      mutableMapOf<KSClassDeclaration, Set<KSClassDeclaration>>().apply {
        contributesEntitiesClasses
          .groupBy { entity ->
            // grouping by database class extracted from the argument ContributesRoomDBVersion::roomDB
            entity.accept(contributesVisitor, Unit) as KSClassDeclaration
          }.forEach { (key, value) ->
            this[key] = value.toSet()
          }
      }

    // retrieve the set of entities (with their transitive) for each Room database annotated with HashingRoomDBVersion
    val entitiesForDatabase =
      mutableMapOf<KSClassDeclaration, Set<KSClassDeclaration>>().apply {
        databaseResolvedClasses.forEach { roomDB ->
          roomDB.accept(databasesVisitor, Unit)?.let {
            this[roomDB] = it
          }
        }
      }

    // for each database, merge their entities with the contributes already discovered
    val mergedEntities: Map<KSClassDeclaration, Set<KSClassDeclaration>> =
      (entitiesForDatabase.keys + contributesEntitiesForDatabase.keys).associateWith { key ->
        (entitiesForDatabase[key] ?: emptySet()) + (contributesEntitiesForDatabase[key] ?: emptySet())
      }

    // generate an Output for each database, computing hash value from the set of entities (+ contributes + nested) found in the previous steps
    val hashingOutput = HashingOutput(logger)
    mergedEntities.entries.forEach {
      hashingOutput.generate(it.key, it.value)?.let { output ->
        outputs.add(output)
      }
    }

    return emptyList()
  }

  override fun finish() {
    codeGenerator
      .createNewFileByPath(
        dependencies =
          Dependencies(
            aggregating = false,
            sources = databaseResolvedClasses.mapNotNull { it.containingFile }.toTypedArray(),
          ),
        path = "com/alecarnevale/diplomatico/results/report",
        extensionName = "csv",
      ).use { stream ->
        OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
          outputs.forEach { output ->
            writer.appendLine("${output.qualifiedName},${output.hash}")
          }
        }
      }
  }
}
