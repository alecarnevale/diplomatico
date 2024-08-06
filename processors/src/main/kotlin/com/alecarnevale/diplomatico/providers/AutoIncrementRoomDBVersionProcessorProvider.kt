package com.alecarnevale.diplomatico.providers

import com.alecarnevale.diplomatico.processors.AutoIncrementRoomDBVersionProcessor
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class AutoIncrementRoomDBVersionProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    AutoIncrementRoomDBVersionProcessor(logger = environment.logger, codeGenerator = environment.codeGenerator)
}
