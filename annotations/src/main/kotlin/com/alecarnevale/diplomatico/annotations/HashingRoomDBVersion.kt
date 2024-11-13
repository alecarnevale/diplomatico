package com.alecarnevale.diplomatico.annotations

import kotlin.reflect.KClass

/**
 * When annotates a Room database, a KSP processor will generate an hash for it based on the entities stored.
 * Hashes for each annotated databases are listed in the same report.
 *
 * @param contributes works in the same way of [ContributesRoomDBVersion] annotation, any referenced class will be used as input for the hashing function.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HashingRoomDBVersion(
  val contributes: Array<KClass<*>> = [],
)
