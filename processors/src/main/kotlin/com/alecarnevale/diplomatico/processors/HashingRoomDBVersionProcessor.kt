package com.alecarnevale.diplomatico.processors

import com.alecarnevale.diplomatico.api.HashingRoomDBVersion
import com.alecarnevale.diplomatico.visitor.HashingOutput
import com.alecarnevale.diplomatico.visitor.HashingRoomDBVersionVisitor
import com.google.devtools.ksp.containingFile
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
  private var databaseResolvedSymbols = setOf<KSAnnotated>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val visitor = HashingRoomDBVersionVisitor(resolver = resolver, logger = logger)

    val annotationNameForDatabase = HashingRoomDBVersion::class.qualifiedName ?: return emptyList()

    databaseResolvedSymbols = resolver.getSymbolsWithAnnotation(annotationNameForDatabase).toSet()

    if (databaseResolvedSymbols.isEmpty()) {
      return emptyList()
    }

    // retrieve the set of entities (with their transitive) for each Room database annotated with HashingRoomDBVersion
    val entitiesForDatabase =
      mutableMapOf<KSAnnotated, Set<KSClassDeclaration>>().apply {
        databaseResolvedSymbols.forEach { roomDB ->
          roomDB.accept(visitor, Unit)?.let {
            this[roomDB] = it
          }
        }
      }

    // generate an Output for each database, computing hash value from the set of entities found in the previous step
    val hashingOutput = HashingOutput(resolver, logger)
    entitiesForDatabase.entries.forEach {
      hashingOutput.generate(it.key as KSClassDeclaration, it.value)?.let { output ->
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
            sources = databaseResolvedSymbols.mapNotNull { it.containingFile }.toTypedArray(),
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
