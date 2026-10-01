package com.vajrax.server.auth

import com.vajrax.server.config.AppConfig
import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Argon2id password hashes in the standard PHC string format
 * (`$argon2id$v=19$m=19456,t=2,p=1$<salt>$<hash>`), so the cost can be raised later and old hashes
 * are upgraded at the next sign-in ([needsRehash]).
 */
class PasswordHasher(private val cost: AppConfig.PasswordHashing) {
    private val random = SecureRandom()

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(password, salt, cost.memoryKb, cost.iterations, cost.parallelism)
        return "\$argon2id\$v=19\$m=${cost.memoryKb},t=${cost.iterations},p=${cost.parallelism}\$${b64(
            salt
        )}\$${b64(hash)}"
    }

    fun verify(password: String, phc: String): Boolean {
        val parsed = parse(phc) ?: return false
        val actual =
            derive(password, parsed.salt, parsed.memoryKb, parsed.iterations, parsed.parallelism, parsed.hash.size)
        return MessageDigest.isEqual(actual, parsed.hash)
    }

    fun needsRehash(phc: String): Boolean {
        val parsed = parse(phc) ?: return true
        return parsed.memoryKb != cost.memoryKb || parsed.iterations != cost.iterations || parsed.parallelism != cost.parallelism
    }

    /** Spends the same time as a real check so unknown emails can't be told apart by timing. */
    fun burn(password: String) {
        derive(password, DUMMY_SALT, cost.memoryKb, cost.iterations, cost.parallelism)
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        memoryKb: Int,
        iterations: Int,
        parallelism: Int,
        length: Int = HASH_BYTES
    ): ByteArray {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withMemoryAsKB(memoryKb)
            .withIterations(iterations)
            .withParallelism(parallelism)
            .withSalt(salt)
            .build()
        val out = ByteArray(length)
        Argon2BytesGenerator().apply { init(params) }.generateBytes(password.toCharArray(), out)
        return out
    }

    private class Parsed(val memoryKb: Int, val iterations: Int, val parallelism: Int, val salt: ByteArray, val hash: ByteArray)

    private fun parse(phc: String): Parsed? = runCatching {
        // ["", "argon2id", "v=19", "m=..,t=..,p=..", salt, hash]
        val parts = phc.split('$')
        if (parts.size != PHC_PARTS || parts[1] != "argon2id") return null
        val p = parts[COSTS].split(',').associate { it.substringBefore('=') to it.substringAfter('=').toInt() }
        Parsed(p.getValue("m"), p.getValue("t"), p.getValue("p"), unb64(parts[SALT]), unb64(parts[HASH]))
    }.getOrNull()

    private fun b64(bytes: ByteArray) = Base64.getEncoder().withoutPadding().encodeToString(bytes)
    private fun unb64(text: String) = Base64.getDecoder().decode(text)

    private companion object {
        const val PHC_PARTS = 6
        const val COSTS = 3
        const val SALT = 4
        const val HASH = 5
        const val SALT_BYTES = 16
        const val HASH_BYTES = 32
        val DUMMY_SALT = ByteArray(SALT_BYTES) { it.toByte() }
    }
}
