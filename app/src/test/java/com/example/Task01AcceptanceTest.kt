package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.Firm
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Task01AcceptanceTest {

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
    fun testBusinessProfileCreationAndQuery() = runBlocking {
        // Run seedDatabase
        AppDatabase.seedDatabase(db)

        val activeProfiles = db.businessProfileDao().getAllActive().first()
        assertEquals(1, activeProfiles.size)
        val profile = activeProfiles[0]
        assertEquals(1L, profile.id)
        assertEquals(Firm.LEGAL_NAME, profile.legalName)
        assertEquals(Firm.BRAND_NAME, profile.brandName)
        assertEquals(Firm.ADDRESS_ONE_LINE, profile.address)
        assertEquals(Firm.GSTIN, profile.gstin)
        assertEquals(Firm.ACCOUNT_NUMBER, profile.bankAccountNo)
        assertEquals(Firm.IFSC, profile.ifsc)
        assertEquals(Firm.SIGNED_BY_PERSON, profile.signatoryName)
    }
}
