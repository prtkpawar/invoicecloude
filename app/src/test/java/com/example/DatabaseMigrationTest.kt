package com.example

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {

    @Test
    fun testMigrationFromVersion1ToCurrent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration.db"
        context.deleteDatabase(dbName)

        // Create a V1 database
        val factory = FrameworkSQLiteOpenHelperFactory()
        val config = Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE IF NOT EXISTS `company` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `email` TEXT NOT NULL, `gstin` TEXT NOT NULL, `placeOfSupply` TEXT NOT NULL, `bankName` TEXT NOT NULL, `accountName` TEXT NOT NULL, `accountNumber` TEXT NOT NULL, `ifsc` TEXT NOT NULL, `signatoryName` TEXT NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `customers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `mobile` TEXT NOT NULL, `village` TEXT NOT NULL, `address` TEXT NOT NULL, `city` TEXT NOT NULL, `state` TEXT NOT NULL, `pincode` TEXT NOT NULL, `notes` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `documents` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `docType` TEXT NOT NULL, `docNo` TEXT NOT NULL, `docDate` TEXT NOT NULL, `validTill` TEXT NOT NULL, `dueDate` TEXT NOT NULL, `customerId` INTEGER NOT NULL, `kw` REAL NOT NULL, `ratePerKw` REAL NOT NULL, `pricingMode` TEXT NOT NULL, `gstMode` TEXT NOT NULL, `gstPercent` REAL NOT NULL, `taxable` REAL NOT NULL, `cgst` REAL NOT NULL, `sgst` REAL NOT NULL, `total` REAL NOT NULL, `amountWords` TEXT NOT NULL, `note` TEXT NOT NULL, `terms` TEXT NOT NULL, `status` TEXT NOT NULL, `parentEstimateId` INTEGER, `pdfPath` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_documents_docNo` ON `documents` (`docNo`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_customerId` ON `documents` (`customerId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_docType` ON `documents` (`docType`)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `doc_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `docId` INTEGER NOT NULL, `label` TEXT NOT NULL, `description` TEXT NOT NULL, `hsn` TEXT NOT NULL, `qty` REAL NOT NULL, `unit` TEXT NOT NULL, `rate` REAL NOT NULL, `amount` REAL NOT NULL, `sortOrder` INTEGER NOT NULL, FOREIGN KEY(`docId`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_doc_items_docId` ON `doc_items` (`docId`)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `item_master` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `label` TEXT NOT NULL, `description` TEXT NOT NULL, `hsn` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `docId` INTEGER NOT NULL, `payDate` TEXT NOT NULL, `amount` REAL NOT NULL, `mode` TEXT NOT NULL, `reference` TEXT NOT NULL, `note` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, FOREIGN KEY(`docId`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_docId` ON `payments` (`docId`)")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val helper = factory.create(config)
        val writableDb = helper.writableDatabase
        writableDb.close()

        // Now open with Room using migrations!
        val roomDb = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8
            )
            .build()

        val parties = roomDb.partyDao().getAllPartiesFlow(1L)
        assertNotNull(parties)
        // Force open and validation
        roomDb.openHelper.writableDatabase
        roomDb.close()
    }
}
