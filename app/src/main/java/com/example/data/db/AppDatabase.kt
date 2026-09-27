package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BusinessProfileDao
import com.example.data.entity.BusinessProfile
import com.example.data.model.AppSetting
import com.example.data.model.Company
import com.example.data.model.Doc
import com.example.data.model.DocItem
import com.example.data.model.Firm
import com.example.data.model.ItemMaster
import com.example.data.model.Party
import com.example.data.model.PartyCategory
import com.example.data.model.Payment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Company::class,
        Party::class,
        Doc::class,
        DocItem::class,
        ItemMaster::class,
        Payment::class,
        AppSetting::class,
        BusinessProfile::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun partyDao(): PartyDao
    fun customerDao(): PartyDao = partyDao()
    abstract fun docDao(): DocDao
    abstract fun docItemDao(): DocItemDao
    abstract fun itemMasterDao(): ItemMasterDao
    abstract fun paymentDao(): PaymentDao
    abstract fun settingDao(): SettingDao
    abstract fun businessProfileDao(): BusinessProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents ADD COLUMN discount REAL NOT NULL DEFAULT 0.0")
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `business_profile` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `legalName` TEXT NOT NULL,
                        `brandName` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `state` TEXT NOT NULL,
                        `pincode` TEXT NOT NULL,
                        `gstin` TEXT,
                        `pan` TEXT,
                        `bankAccountNo` TEXT,
                        `ifsc` TEXT,
                        `upiVpa` TEXT,
                        `signatoryName` TEXT,
                        `logoPath` TEXT,
                        `signaturePath` TEXT,
                        `businessTemplateId` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                val now = System.currentTimeMillis()
                val escapedLegal = Firm.LEGAL_NAME.replace("'", "''")
                val escapedBrand = Firm.BRAND_NAME.replace("'", "''")
                val escapedAddr = Firm.ADDRESS_ONE_LINE.replace("'", "''")
                val escapedState = Firm.STATE.replace("'", "''")
                val escapedSign = Firm.SIGNED_BY_PERSON.replace("'", "''")

                db.execSQL("""
                    INSERT OR IGNORE INTO `business_profile` (
                        `id`, `legalName`, `brandName`, `address`, `city`, `state`, `pincode`,
                        `gstin`, `pan`, `bankAccountNo`, `ifsc`, `upiVpa`, `signatoryName`,
                        `logoPath`, `signaturePath`, `businessTemplateId`, `isActive`, `createdAt`
                    ) VALUES (
                        1,
                        '$escapedLegal',
                        '$escapedBrand',
                        '$escapedAddr',
                        'Dhule',
                        '$escapedState',
                        '424306',
                        '${Firm.GSTIN}',
                        'BAZPT3492D',
                        '${Firm.ACCOUNT_NUMBER}',
                        '${Firm.IFSC}',
                        '324201010510532@ubin',
                        '$escapedSign',
                        NULL,
                        NULL,
                        1,
                        1,
                        $now
                    )
                """.trimIndent())
            }
        }

        internal val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE documents ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE doc_items ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE payments ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE item_master ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE settings ADD COLUMN businessId INTEGER NOT NULL DEFAULT 1")

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_customers_businessId` ON `customers` (`businessId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_businessId` ON `documents` (`businessId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_doc_items_businessId` ON `doc_items` (`businessId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payments_businessId` ON `payments` (`businessId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_master_businessId` ON `item_master` (`businessId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_settings_businessId` ON `settings` (`businessId`)")
            }
        }

        private fun recreateDocumentsTableWithPartyForeignKey(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `documents_temp` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `businessId` INTEGER NOT NULL DEFAULT 1,
                    `docType` TEXT NOT NULL,
                    `docNo` TEXT NOT NULL,
                    `docDate` TEXT NOT NULL,
                    `validTill` TEXT NOT NULL,
                    `dueDate` TEXT NOT NULL,
                    `customerId` INTEGER NOT NULL,
                    `kw` REAL NOT NULL,
                    `ratePerKw` REAL NOT NULL,
                    `pricingMode` TEXT NOT NULL,
                    `gstMode` TEXT NOT NULL,
                    `gstPercent` REAL NOT NULL,
                    `discount` REAL NOT NULL DEFAULT 0.0,
                    `taxable` REAL NOT NULL,
                    `cgst` REAL NOT NULL,
                    `sgst` REAL NOT NULL,
                    `total` REAL NOT NULL,
                    `amountWords` TEXT NOT NULL,
                    `note` TEXT NOT NULL,
                    `terms` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `parentEstimateId` INTEGER,
                    `pdfPath` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    FOREIGN KEY(`customerId`) REFERENCES `parties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())

            db.execSQL("""
                INSERT INTO `documents_temp` (
                    `id`, `businessId`, `docType`, `docNo`, `docDate`, `validTill`, `dueDate`, `customerId`,
                    `kw`, `ratePerKw`, `pricingMode`, `gstMode`, `gstPercent`, `discount`, `taxable`, `cgst`,
                    `sgst`, `total`, `amountWords`, `note`, `terms`, `status`, `parentEstimateId`, `pdfPath`,
                    `createdAt`, `updatedAt`
                )
                SELECT
                    `id`, `businessId`, `docType`, `docNo`, `docDate`, `validTill`, `dueDate`, `customerId`,
                    `kw`, `ratePerKw`, `pricingMode`, `gstMode`, `gstPercent`, `discount`, `taxable`, `cgst`,
                    `sgst`, `total`, `amountWords`, `note`, `terms`, `status`, `parentEstimateId`, `pdfPath`,
                    `createdAt`, `updatedAt`
                FROM `documents`
            """.trimIndent())

            db.execSQL("DROP TABLE `documents`")
            db.execSQL("ALTER TABLE `documents_temp` RENAME TO `documents`")

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_documents_docNo` ON `documents` (`docNo`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_customerId` ON `documents` (`customerId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_docType` ON `documents` (`docType`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_businessId` ON `documents` (`businessId`)")
        }

        internal val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `customers` RENAME TO `parties`")
                db.execSQL("DROP INDEX IF EXISTS `index_customers_businessId`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_businessId` ON `parties` (`businessId`)")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `phone` TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE `parties` SET `phone` = `mobile` WHERE `phone` = '' OR `phone` IS NULL")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `email` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `gstin` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `consumerNumber` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `sanctionLoad` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'CUSTOMER'")
                db.execSQL("ALTER TABLE `parties` ADD COLUMN `photoPath` TEXT")

                recreateDocumentsTableWithPartyForeignKey(db)
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'SOLAR_PANEL'")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `unit` TEXT NOT NULL DEFAULT 'NOS'")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `taxRate` REAL NOT NULL DEFAULT 18.0")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `rate` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `purchasePrice` REAL")
            }
        }

        internal val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_customers_businessId`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_businessId` ON `parties` (`businessId`)")
                recreateDocumentsTableWithPartyForeignKey(db)
            }
        }

        internal val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `businessType` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `mrp` REAL")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `barcode` TEXT")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `batchNo` TEXT")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `expiry` TEXT")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `warranty` TEXT")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `brand` TEXT")
                db.execSQL("ALTER TABLE `item_master` ADD COLUMN `extraAttributes` TEXT")
            }
        }

        internal val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `documents` ADD COLUMN `whatsappShareCount` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swami_solar.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            // 0. Seed BusinessProfile 1: Solar & Renewable
            database.businessProfileDao().insert(
                BusinessProfile(
                    id = 1L,
                    legalName = Firm.LEGAL_NAME,
                    brandName = Firm.BRAND_NAME,
                    address = Firm.ADDRESS_ONE_LINE,
                    city = "Dhule",
                    state = Firm.STATE,
                    pincode = "424306",
                    gstin = Firm.GSTIN,
                    pan = "BAZPT3492D",
                    bankAccountNo = Firm.ACCOUNT_NUMBER,
                    ifsc = Firm.IFSC,
                    upiVpa = "324201010510532@ubin",
                    signatoryName = Firm.SIGNED_BY_PERSON,
                    logoPath = null,
                    signaturePath = null,
                    businessTemplateId = 1L,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
            )

            // 0b. Seed BusinessProfile 2: Kirana & Grocery Store
            database.businessProfileDao().insert(
                BusinessProfile(
                    id = 2L,
                    legalName = "Mahalaxmi Super Market",
                    brandName = "Mahalaxmi Kirana Stores",
                    address = "Shop No. 4, Market Yard, Station Road",
                    city = "Dhule",
                    state = "Maharashtra",
                    pincode = "424001",
                    gstin = "27CCCCC2222C1Z9",
                    pan = "ABCDE1234F",
                    bankAccountNo = "919010012345678",
                    ifsc = "UTIB0000123",
                    upiVpa = "mahalaxmi.kirana@okaxis",
                    signatoryName = "Proprietor",
                    logoPath = null,
                    signaturePath = null,
                    businessTemplateId = 3L,
                    isActive = true,
                    createdAt = System.currentTimeMillis() + 1
                )
            )

            // 1. Seed Company
            database.companyDao().insert(
                Company(
                    id = 1,
                    name = "swami construction",
                    address = "BRAHMAN GALLI SHARDA TYPING PIMPALNER , PIMPALNER /DHULE , Maharashtra-424306, India",
                    email = "solarswamiconstruction@gmail.com",
                    gstin = "27BAZPT3492D1ZY",
                    placeOfSupply = "Maharashtra (27)",
                    bankName = "UNION BANK OF INDIA",
                    accountName = "SWAMI CONSTRUCTION",
                    accountNumber = "324201010510532",
                    ifsc = "UBIN0532428",
                    signatoryName = "Mr. Rohit Tatar"
                )
            )

            // 2. Seed Item Master (A to H)
            val defaultItems = listOf(
                Triple("Modules make NDCR aadani if any one avilable as per panel PV Power", com.example.data.model.ItemCategory.SOLAR_PANEL, com.example.data.model.ItemUnit.WATT to 13.8),
                Triple("GI Mounting Structure GI (APL Apollo) & Spares Parts", com.example.data.model.ItemCategory.STRUCTURE, com.example.data.model.ItemUnit.SET to 18.0),
                Triple("{KW} Single Phase Grid Connected sting inverter Make polycab", com.example.data.model.ItemCategory.INVERTER, com.example.data.model.ItemUnit.NOS to 18.0),
                Triple("Cabling (Polycab) & Accessories till the point of distribution", com.example.data.model.ItemCategory.CABLE, com.example.data.model.ItemUnit.MTR to 18.0),
                Triple("ACDB / DCDB Box", com.example.data.model.ItemCategory.ACCESSORY, com.example.data.model.ItemUnit.NOS to 18.0),
                Triple("Earthing Kit", com.example.data.model.ItemCategory.ACCESSORY, com.example.data.model.ItemUnit.SET to 18.0),
                Triple("Lightning Arrestor", com.example.data.model.ItemCategory.ACCESSORY, com.example.data.model.ItemUnit.NOS to 18.0),
                Triple("Net Meter make Schneider (L&T)", com.example.data.model.ItemCategory.ACCESSORY, com.example.data.model.ItemUnit.NOS to 18.0)
            )
            val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H")
            val itemEntities = defaultItems.mapIndexed { index, (desc, category, unitTax) ->
                ItemMaster(
                    label = labels[index],
                    description = desc,
                    hsn = "85414300",
                    category = category,
                    unit = unitTax.first,
                    taxRate = unitTax.second,
                    isActive = true,
                    sortOrder = index
                )
            }
            database.itemMasterDao().insertAll(itemEntities)

            // 2b. Seed Kirana Item Master (Grocery Staples)
            val kiranaItems = listOf(
                ItemMaster(label = "1", description = "Basmati Rice (5kg)", hsn = "10063020", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.BAG, taxRate = 5.0, rate = 480.0, businessId = 2L, isActive = true, sortOrder = 0),
                ItemMaster(label = "2", description = "Chakki Fresh Atta (10kg)", hsn = "11010000", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.BAG, taxRate = 0.0, rate = 390.0, businessId = 2L, isActive = true, sortOrder = 1),
                ItemMaster(label = "3", description = "Refined Sunflower Oil (1L)", hsn = "15121910", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.LTR, taxRate = 5.0, rate = 145.0, businessId = 2L, isActive = true, sortOrder = 2),
                ItemMaster(label = "4", description = "Pure Cow Ghee (1L)", hsn = "04059020", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.LTR, taxRate = 12.0, rate = 620.0, businessId = 2L, isActive = true, sortOrder = 3),
                ItemMaster(label = "5", description = "Toor Dal (1kg)", hsn = "07136000", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.KG, taxRate = 0.0, rate = 160.0, businessId = 2L, isActive = true, sortOrder = 4),
                ItemMaster(label = "6", description = "Madhur Clean Sugar (1kg)", hsn = "17019990", category = com.example.data.model.ItemCategory.ACCESSORY, unit = com.example.data.model.ItemUnit.KG, taxRate = 5.0, rate = 46.0, businessId = 2L, isActive = true, sortOrder = 5)
            )
            database.itemMasterDao().insertAll(kiranaItems)

            // 3. Seed Default Terms & Settings
            val defaultTerms = com.example.data.model.DEFAULT_ESTIMATE_TERMS

            val defaultSettings = listOf(
                AppSetting("next_est_no", "69"),
                AppSetting("next_inv_no", "1"),
                AppSetting("est_prefix", "EST-"),
                AppSetting("inv_prefix", "INV-"),
                AppSetting("default_terms", defaultTerms),
                AppSetting("default_note", "Bank Loan proposal"),
                AppSetting("default_rate_per_kw", "60000"),
                AppSetting("default_gst_percent", "5"),
                AppSetting(
                    "msg_estimate",
                    "Namaskar {customer},\nYour solar estimate {docno} for {kw} kW is attached.\nTotal: {total}\n\n- swami construction"
                ),
                AppSetting(
                    "msg_invoice",
                    "Namaskar {customer},\nInvoice {docno} is attached.\nTotal: {total} | Paid: {paid} | Balance: {pending}\n\n- swami construction"
                ),
                AppSetting(
                    "msg_reminder",
                    "Namaskar {customer},\nGentle reminder for invoice {docno}.\nTotal: {total}\nPaid: {paid}\nPending: {pending}\nKindly arrange the payment.\n\n- swami construction"
                ),
                AppSetting(
                    "msg_receipt",
                    "Namaskar {customer},\nWe have received {amount} against invoice {docno}.\nBalance now: {pending}\nThank you.\n\n- swami construction"
                )
            )
            database.settingDao().insertAll(defaultSettings)

            // 4. Seed initial customer & sample estimate matching EST-00068
            val custId = database.partyDao().insert(
                Party(
                    name = "Nausyad Dada",
                    phone = "9890951234",
                    village = "Pimpalner",
                    address = "Brahman Galli, Pimpalner / Dhule",
                    city = "Dhule",
                    state = "Maharashtra",
                    pincode = "424306",
                    category = PartyCategory.CUSTOMER
                )
            ).toInt()

            val sampleEst = Doc(
                docType = "ESTIMATE",
                docNo = "EST-00068",
                docDate = "2026-09-17",
                validTill = "2026-09-24",
                customerId = custId,
                kw = 5.0,
                ratePerKw = 60000.0,
                pricingMode = "ROUND_TOTAL",
                gstMode = "GST_5",
                gstPercent = 5.0,
                taxable = 176190.48,
                cgst = 4404.76,
                sgst = 4404.76,
                total = 185000.0,
                amountWords = "One Lakh Eighty Five Thousand Rupees",
                note = "Bank Loan proposal",
                terms = defaultTerms,
                status = "ESTIMATE"
            )
            val estId = database.docDao().insert(sampleEst).toInt()

            val docItems = defaultItems.mapIndexed { index, (desc, _, _) ->
                DocItem(
                    docId = estId,
                    label = labels[index],
                    description = desc,
                    hsn = "85414300",
                    qty = 1.0,
                    unit = "set",
                    rate = if (index == 0) 176190.48 else 0.0,
                    amount = if (index == 0) 176190.48 else 0.0,
                    sortOrder = index
                )
            }
            database.docItemDao().insertAll(docItems)
        }
    }
}
