package com.alecarnevale.diplomatico.api

/**
 * When annotates a Room database, a KSP processor will generate an hash for it based on the entities stored.
 * Hashes for each annotated databases are listed in the same report.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoIncrementRoomDBVersion
