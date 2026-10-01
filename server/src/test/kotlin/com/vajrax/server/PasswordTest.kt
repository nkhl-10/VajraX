package com.vajrax.server

import com.vajrax.server.auth.PasswordHasher
import com.vajrax.server.auth.PasswordPolicy
import com.vajrax.server.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordTest {
    private val hasher = PasswordHasher(AppConfig.PasswordHashing(memoryKb = 1024, iterations = 1))

    @Test
    fun hashesAreSaltedPhcStringsThatVerify() {
        val a = hasher.hash("river stone lamp")
        val b = hasher.hash("river stone lamp")
        assertTrue(a.startsWith("\$argon2id\$v=19\$m=1024,t=1,p=1\$"))
        assertTrue(a != b)
        assertTrue(hasher.verify("river stone lamp", a))
        assertFalse(hasher.verify("river stone lamb", a))
        assertFalse(hasher.verify("anything", "not a hash"))
    }

    @Test
    fun strongerSettingsAskForARehash() {
        val old = hasher.hash("river stone lamp")
        assertFalse(hasher.needsRehash(old))
        assertTrue(PasswordHasher(AppConfig.PasswordHashing()).needsRehash(old))
    }

    @Test
    fun policyIsAboutLengthAndObviousChoices() {
        assertNull(PasswordPolicy.problem("river stone lamp", "sam@example.com"))
        assertNull(PasswordPolicy.problem("Kx8!rQ2#pL", "sam@example.com"))
        assertNotNull(PasswordPolicy.problem("short", "sam@example.com"))
        assertNotNull(PasswordPolicy.problem("password123", "sam@example.com"))
        assertNotNull(PasswordPolicy.problem("aaaaaaaaaaaa", "sam@example.com"))
        assertNotNull(PasswordPolicy.problem("abcdefghijkl", "sam@example.com"))
        assertNotNull(PasswordPolicy.problem("samuel-2026-xyz", "samuel@example.com"))
    }
}
