package com.sanskar.eventhive.presentation.permission

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PermissionHelperTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var permissionHelper: PermissionHelper

    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        permissionHelper = PermissionHelper(firestore)
    }

    @Test
    fun `refresh with empty uid resets to default permissions`() = runTest {
        permissionHelper.refresh("")
        
        assertFalse(permissionHelper.hasPermission("canCreateEvent"))
        assertFalse(permissionHelper.hasPermission("canModerateChat"))
    }

    @Test
    fun `refresh with valid uid updates permissions from firestore`() = runTest {
        val uid = "user123"
        val snapshot = mockk<DocumentSnapshot>()
        val mockPermissions = mapOf("canCreateEvent" to true, "canModerateChat" to true)
        
        every { snapshot.get("effectivePermissions") } returns mockPermissions
        every { firestore.collection("users").document(uid).get() } returns Tasks.forResult(snapshot)

        permissionHelper.refresh(uid)

        assertTrue(permissionHelper.hasPermission("canCreateEvent"))
        assertTrue(permissionHelper.hasPermission("canModerateChat"))
        assertFalse(permissionHelper.hasPermission("canDeleteEvent")) // default remains false
    }

    @Test
    fun `refresh with null effectivePermissions resets to defaults`() = runTest {
        val uid = "user123"
        val snapshot = mockk<DocumentSnapshot>()
        
        every { snapshot.get("effectivePermissions") } returns null
        every { firestore.collection("users").document(uid).get() } returns Tasks.forResult(snapshot)

        permissionHelper.refresh(uid)

        assertFalse(permissionHelper.hasPermission("canCreateEvent"))
    }
}
