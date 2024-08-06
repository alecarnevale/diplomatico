package com.alecarnevale.diplomatico.api

/**
 * When annotates a Room Entity, a KSP processor is aware to generate a report
 * where store an incremental version of the corresponding Room database.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AutoIncrementRoomDBVersion
