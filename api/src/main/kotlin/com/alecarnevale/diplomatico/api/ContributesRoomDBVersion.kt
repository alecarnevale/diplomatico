package com.alecarnevale.diplomatico.api

import kotlin.reflect.KClass

/**
 * When annotates a class, the processor will calculate its hash value when processing the Room database defined as [roomDB].
 *
 * @param roomDB the Room DB class for which this class must be considered part of its hashing function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ContributesRoomDBVersion(
  val roomDB: KClass<*>,
)
