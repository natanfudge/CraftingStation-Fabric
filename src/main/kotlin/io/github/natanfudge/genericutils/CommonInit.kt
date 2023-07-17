package io.github.natanfudge.genericutils

/**
 * Functions that should only be called in CommonInit should be marked as context(CommonInit).
 * Then the mod's onInitialize() override should do with(CommonInit).
 */
object CommonInit