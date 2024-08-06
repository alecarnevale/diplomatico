package com.alecarnevale.diplomatico.visitor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Returns file content hash and fully qualified name of the specified class.
 */
internal class AutoIncrementRoomDBVersionVisitor(
  private val logger: KSPLogger,
) : KSEmptyVisitor<Unit, AutoIncrementRoomDBVersionVisitor.Output?>() {
  override fun visitClassDeclaration(
    classDeclaration: KSClassDeclaration,
    data: Unit,
  ): Output? =
    classDeclaration.containingFile?.filePath?.let { filePath ->
      val qualifiedName = classDeclaration.qualifiedName?.asString()
      if (qualifiedName == null) {
        logger.error("Error while get qualifiedName for $classDeclaration")
        return null
      }
      Output(
        hash = hashingFile(filePath),
        qualifiedName = qualifiedName,
      )
    } ?: run {
      logger.error("Error while parsing $classDeclaration")
      null
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
}
