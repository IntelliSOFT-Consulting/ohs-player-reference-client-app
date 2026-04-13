package dev.ohs.player.reference.client.app.security

import dev.ohs.player.reference.client.app.MyApplication
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy


actual val platformKSafe = KSafe(
    context = MyApplication.getAppContext(), // You need to pass the Android Context
    fileName = "user_prefs",
    memoryPolicy = KSafeMemoryPolicy.PLAIN_TEXT
)

actual val platformEncryptedKSafe = KSafe(
    context = MyApplication.getAppContext(),
    fileName = "secure_vault"
)