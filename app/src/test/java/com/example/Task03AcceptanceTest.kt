package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Party
import com.example.data.model.PartyCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Task03AcceptanceTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testPartyEntityAndCategoryDefaults() = runBlocking {
        // Seed database
        AppDatabase.seedDatabase(db)

        // Verify parties query
        val parties = db.partyDao().getAllPartiesFlow(1L).first()
        assertEquals(1, parties.size)
        val defaultParty = parties[0]
        assertEquals("Nausyad Dada", defaultParty.name)
        assertEquals("9890951234", defaultParty.phone)
        assertEquals("9890951234", defaultParty.mobile) // backwards compatibility getter
        assertEquals(PartyCategory.CUSTOMER, defaultParty.category)

        // Insert a vendor party
        val vendor = Party(
            businessId = 1L,
            name = "Tata Power Solar Vendor",
            phone = "9876543210",
            category = PartyCategory.VENDOR
        )
        val vendorId = db.partyDao().insert(vendor)
        assertTrue(vendorId > 0)

        // Query by category
        val customersOnly = db.partyDao().getPartiesByCategoryFlow(PartyCategory.CUSTOMER, 1L).first()
        assertEquals(1, customersOnly.size)
        assertEquals("Nausyad Dada", customersOnly[0].name)

        val vendorsOnly = db.partyDao().getPartiesByCategoryFlow(PartyCategory.VENDOR, 1L).first()
        assertEquals(1, vendorsOnly.size)
        assertEquals("Tata Power Solar Vendor", vendorsOnly[0].name)

        // Search parties
        val searchResult = db.partyDao().searchPartiesFlow("Tata", 1L).first()
        assertEquals(1, searchResult.size)
        assertEquals("Tata Power Solar Vendor", searchResult[0].name)
    }
}
