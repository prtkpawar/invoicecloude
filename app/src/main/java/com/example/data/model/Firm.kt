package com.example.data.model

/**
 * FIXED FIRM DETAILS.
 *
 * These are compiled into the app. They are NOT stored in the database and
 * there is no screen anywhere that can change them, so a restore, a bad tap
 * or a corrupted row can never alter what prints on a legal document.
 * To change them you edit this file and rebuild the APK.
 */
object Firm {
    const val BRAND_NAME = "SWAMI SOLAR"
    const val BRAND_TAGLINE = "A SWAMI CONSTRUCTION FIRM"
    const val LEGAL_NAME = "SWAMI CONSTRUCTION"

    const val ADDRESS = "Brahman Galli, Sharda Typing, Pimpalner, Pimpalner / Dhule,\nMaharashtra - 424306, India"
    const val ADDRESS_ONE_LINE = "Brahman Galli, Sharda Typing, Pimpalner, Pimpalner / Dhule, Maharashtra - 424306, India"
    const val EMAIL = "solarswamiconstruction@gmail.com"
    const val GSTIN = "27BAZPT3492D1ZY"
    const val PLACE_OF_SUPPLY = "Maharashtra (27)"
    const val STATE = "Maharashtra"

    const val BANK_NAME = "UNION BANK OF INDIA"
    const val ACCOUNT_NAME = "SWAMI CONSTRUCTION"
    const val ACCOUNT_NUMBER = "324201010510532"
    const val IFSC = "UBIN0532428"

    // Printed in the digital signature panel
    
    const val SIGNED_BY_PERSON = "Mr. Rohit Tatar"

    const val DEVELOPER = "Pratik Pawar"
    const val DEVELOPER_EMAIL = "Pawar4prati@gmail.com"
    const val COPYRIGHT_YEAR = "2026"
    const val APP_VERSION = "1.2.0"
}
