package com.alecarnevale.diplomatico.processors

import com.alecarnevale.diplomatico.api.AutoIncrementRoomDBVersion
import com.alecarnevale.diplomatico.visitor.AutoIncrementRoomDBVersionVisitor
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Process each [AutoIncrementRoomDBVersion] to generate a report.
 */
internal class AutoIncrementRoomDBVersionProcessor(
  private val logger: KSPLogger,
  private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
  private var outputs = listOf<AutoIncrementRoomDBVersionVisitor.Output>()
  private var resolvedSymbols = setOf<KSAnnotated>()

  override fun process(resolver: Resolver): List<KSAnnotated> {
    val visitor = AutoIncrementRoomDBVersionVisitor(resolver = resolver, logger = logger)

    val annotationName = AutoIncrementRoomDBVersion::class.qualifiedName ?: return emptyList()

    resolvedSymbols = resolver.getSymbolsWithAnnotation(annotationName).toSet()

    if (resolvedSymbols.isEmpty()) {
      return emptyList()
    }

    outputs =
      resolvedSymbols.mapNotNull {
        it.accept(visitor, Unit)
      }

    return emptyList()
  }

  override fun finish() {
    codeGenerator
      .createNewFileByPath(
        dependencies =
          Dependencies(
            aggregating = false,
            sources = resolvedSymbols.mapNotNull { it.containingFile }.toTypedArray(),
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
