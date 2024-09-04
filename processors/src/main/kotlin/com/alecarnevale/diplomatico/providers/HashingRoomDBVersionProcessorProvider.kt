package com.alecarnevale.diplomatico.providers

import com.alecarnevale.diplomatico.processors.HashingRoomDBVersionProcessor
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class HashingRoomDBVersionProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    HashingRoomDBVersionProcessor(logger = environment.logger, codeGenerator = environment.codeGenerator)
}
