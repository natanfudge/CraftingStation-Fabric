package io.github.natanfudge.genericutils

/**
 * Functions that should only be called in ClientInit should be marked as context(ClientInit).
 * Then the mod's onInitializeClient() override should do with(ClientInit).
 */
object ClientInit