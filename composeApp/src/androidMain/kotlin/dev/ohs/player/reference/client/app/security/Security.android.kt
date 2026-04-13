package dev.ohs.player.reference.client.app.security

import dev.ohs.player.reference.client.app.MyApplication
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeMemoryPolicy


actual val platformKSafe by lazy {
    KSafe(
        context = MyApplication.appContext.applicationContext,
        fileName = "user_prefs",
        memoryPolicy = KSafeMemoryPolicy.PLAIN_TEXT
    )
}
actual val platformEncryptedKSafe by lazy {
    KSafe(
        context = MyApplication.appContext.applicationContext,
        fileName = "secure_vault"
    )
}