package com.pm.ridepay.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class TokenStore(context: Context) {


    private val prefs= context.getSharedPreferences("ridepay_session", Context.MODE_PRIVATE)
    fun saveToken(token: String){
        val cipher= Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv=cipher.iv
        val encrypted=cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val combined=iv+encrypted
        prefs.edit()
            .putString(KEY_TOKEN, Base64.encodeToString(combined, Base64.NO_WRAP))
            .apply()
    }

    fun getToken(): String?{
        val stored=prefs.getString(KEY_TOKEN, null)?: return null
        return try{
            val combined= Base64.decode(stored, Base64.NO_WRAP)
            val iv=combined.copyOfRange(0, IV_SIZE)
            val encrypted=combined.copyOfRange(IV_SIZE, combined.size)
            val cipher= Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(TAG_LENGTH_BITS, iv)
            )
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }
        catch (e: Exception){
            clear()
            null
        }
    }

    fun clear(){
        prefs.edit().remove(KEY_TOKEN).apply()
    }


    private fun getOrCreateKey(): SecretKey{
        val keyStore= KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator= KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }


    private companion object{
        const val KEY_ALIAS="ridepay_token_key"
        const val KEY_TOKEN="jwt"
        const val TRANSFORMATION="AES/GCM/NoPadding"
        const val IV_SIZE=12
        const val TAG_LENGTH_BITS=128
    }

}