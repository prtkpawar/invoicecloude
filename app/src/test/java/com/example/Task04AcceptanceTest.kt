package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.ItemCategory
import com.example.data.model.ItemMaster
import com.example.data.model.ItemUnit
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
class Task04AcceptanceTest {

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
    fun testItemMasterNormalizedCategoriesUnitsAndTaxRate() = runBlocking {
        // Seed database
        AppDatabase.seedDatabase(db)

        // Verify seeded items have correct categories and units
        val items = db.itemMasterDao().getActiveItemsFlow(1L).first()
        assertTrue(items.isNotEmpty())

        val solarPanelItem = items.find { it.category == ItemCategory.SOLAR_PANEL }
        assertNotNull(solarPanelItem)
        assertEquals(ItemUnit.WATT, solarPanelItem?.unit)
        assertEquals(13.8, solarPanelItem?.taxRate ?: 0.0, 0.01)

        val inverterItem = items.find { it.category == ItemCategory.INVERTER }
        assertNotNull(inverterItem)
        assertEquals(ItemUnit.NOS, inverterItem?.unit)
        assertEquals(18.0, inverterItem?.taxRate ?: 0.0, 0.01)

        // Insert new item with new fields
        val newItem = ItemMaster(
            businessId = 1L,
            label = "I",
            description = "5kVA Lithium Ferro Phosphate Battery Pack",
            hsn = "85076000",
            category = ItemCategory.BATTERY,
            unit = ItemUnit.SET,
            taxRate = 18.0,
            rate = 125000.0,
            purchasePrice = 98000.0,
            isActive = true,
            sortOrder = 8
        )
        val insertedId = db.itemMasterDao().insert(newItem)
        assertTrue(insertedId > 0)

        // Query by category
        val batteryItems = db.itemMasterDao().getItemsByCategoryFlow(ItemCategory.BATTERY, 1L).first()
        assertEquals(1, batteryItems.size)
        assertEquals("5kVA Lithium Ferro Phosphate Battery Pack", batteryItems[0].description)
        assertEquals(125000.0, batteryItems[0].rate, 0.01)
        assertEquals(98000.0, batteryItems[0].purchasePrice ?: 0.0, 0.01)
        assertEquals(ItemUnit.SET, batteryItems[0].unit)
    }
}
