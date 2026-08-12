package com.example.votingsystem

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object EncryptionManager {
    private const val KEY_ALIAS = "realm_encryption_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    fun getEncryptionKey(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        // Realm requires a 64-byte key for AES-256 encryption. 
        // We pad our 32-byte (256-bit) key to 64 bytes.
        val keyBytes = secretKey.encoded
        val realmKey = ByteArray(64)
        System.arraycopy(keyBytes, 0, realmKey, 0, keyBytes.size)
        System.arraycopy(keyBytes, 0, realmKey, 32, keyBytes.size)
        return realmKey
    }
}
