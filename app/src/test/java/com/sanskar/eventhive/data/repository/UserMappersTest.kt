package com.sanskar.eventhive.data.repository

import com.sanskar.eventhive.data.model.AppRole
import com.sanskar.eventhive.data.model.ClubRole
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UserMappersTest {

    @Test
    fun `toSafeUser maps valid data correctly`() {
        val snapshot = mockk<DocumentSnapshot>()
        val data = mapOf(
            "userId" to "u1",
            "name" to "Sanskar",
            "email" to "sanskar@example.com",
            "appRole" to "ADMIN",
            "collegeId" to "col1",
            "systemRole" to "superAdmin",
            "clubs" to listOf(
                mapOf("clubId" to "club1", "categoryId" to "cat1", "role" to "ADMIN")
            ),
            "effectivePermissions" to mapOf(
                "canCreateEvent" to true
            )
        )

        every { snapshot.exists() } returns true
        every { snapshot.data } returns data
        every { snapshot.id } returns "u1"
        every { snapshot.get(any<String>()) } answers { data[arg(0)] }

        val user = snapshot.toSafeUser()

        assertNotNull(user)
        assertEquals("u1", user?.userId)
        assertEquals("Sanskar", user?.name)
        assertEquals(AppRole.ADMIN, user?.appRole)
        assertEquals("col1", user?.collegeId)
        assertEquals("superAdmin", user?.systemRole)
        assertEquals(1, user?.clubs?.size)
        assertEquals(ClubRole.ADMIN, user?.clubs?.get(0)?.role)
        assertEquals(true, user?.effectivePermissions?.get("canCreateEvent"))
        assertEquals(false, user?.effectivePermissions?.get("canEditEvent")) // Default
    }

    @Test
    fun `toSafeUser handles missing or null fields gracefully`() {
        val snapshot = mockk<DocumentSnapshot>()
        val data = mapOf(
            "name" to "Sanskar"
            // Missing many fields
        )

        every { snapshot.exists() } returns true
        every { snapshot.data } returns data
        every { snapshot.id } returns "u1"
        every { snapshot.get(any<String>()) } answers { data[arg(0)] }

        val user = snapshot.toSafeUser()

        assertNotNull(user)
        assertEquals("u1", user?.userId) // Uses ID if userId blank
        assertEquals("Sanskar", user?.name)
        assertEquals(AppRole.MEMBER, user?.appRole) // Default
        assertEquals("student", user?.systemRole) // Default
        assertEquals(emptyList<Any>(), user?.clubs)
        assertEquals(false, user?.effectivePermissions?.get("canCreateEvent")) // Default
    }

    @Test
    fun `toSafeUser returns null if snapshot does not exist`() {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.exists() } returns false

        val user = snapshot.toSafeUser()

        assertEquals(null, user)
    }
}
