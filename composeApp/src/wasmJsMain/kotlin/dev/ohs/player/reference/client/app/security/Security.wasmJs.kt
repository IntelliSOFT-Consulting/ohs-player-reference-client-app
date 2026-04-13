package dev.ohs.player.reference.client.app.security

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy


// webMain
actual val platformKSafe = KSafe(
    fileName = "user_prefs",
    memoryPolicy = KSafeMemoryPolicy.PLAIN_TEXT
)

actual val platformEncryptedKSafe = KSafe(fileName = "secure_vault")