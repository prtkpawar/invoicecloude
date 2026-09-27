package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.DocType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Task02AcceptanceTest {

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
    fun testBusinessIdFilteringAndRowIntegrity() = runBlocking {
        // Seed default database
        AppDatabase.seedDatabase(db)

        // Verify customers has businessId = 1
        val customers = db.customerDao().getAllCustomersFlow(1L).first()
        assertEquals(1, customers.size)
        assertEquals(1L, customers[0].businessId)
        assertEquals("Nausyad Dada", customers[0].name)

        // Verify documents has businessId = 1
        val docs = db.docDao().getAllDocsFlow(1L).first()
        assertEquals(1, docs.size)
        assertEquals(1L, docs[0].businessId)
        assertEquals("EST-00068", docs[0].docNo)

        // Verify query for non-existent businessId = 999 returns empty list
        val emptyDocs = db.docDao().getAllDocsFlow(999L).first()
        assertEquals(0, emptyDocs.size)

        // Verify item master items have businessId = 1
        val items = db.itemMasterDao().getActiveItemsFlow(1L).first()
        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.businessId == 1L })

        // Verify settings have businessId = 1
        val estPrefix = db.settingDao().getSetting("est_prefix", 1L)
        assertEquals("EST-", estPrefix)
    }
}
