package com.example.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String
)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("mr", "Marathi", "मराठी"),
    LanguageOption("hi", "Hindi", "हिंदी"),
    LanguageOption("gu", "Gujarati", "ગુજરાતી")
)

object AppLanguageManager {
    private const val PREFS_NAME = "app_language_prefs"
    private const val KEY_LANG = "selected_app_language"

    private val _currentLanguageFlow = MutableStateFlow("en")
    val currentLanguageFlow: StateFlow<String> = _currentLanguageFlow.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANG, "en") ?: "en"
        _currentLanguageFlow.value = savedLang
        applySystemLocale(context, savedLang)
    }

    fun setLanguage(context: Context, langCode: String) {
        _currentLanguageFlow.value = langCode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, langCode)
            .apply()

        // Also sync legacy app_settings if any
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", langCode)
            .apply()

        applySystemLocale(context, langCode)
    }

    private fun applySystemLocale(context: Context, langCode: String) {
        try {
            val locale = Locale(langCode)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun get(key: String, lang: String = _currentLanguageFlow.value): String {
        return TRANSLATIONS[lang]?.get(key)
            ?: TRANSLATIONS["en"]?.get(key)
            ?: key
    }

    private val TRANSLATIONS = mapOf(
        "en" to mapOf(
            "app_name" to "BillBook",
            "nav_home" to "Home",
            "nav_docs" to "Docs",
            "nav_more" to "More",
            "nav_settings" to "Settings",
            "greeting_title" to "Good day",
            "greeting_sub" to "Live Billing & Khata Ledger",
            "dev_mode_badge" to "🧪 DEV MODE: UNLOCKED",
            "collected_month" to "Collected this month",
            "pending_receivables" to "Pending Receivables",
            "pending_dues" to "Pending dues",
            "lifetime_billed" to "Lifetime billed",
            "open_proposals" to "Open proposals",
            "create_invoice" to "Create Tax Invoice",
            "record_payment" to "Record Payment",
            "quick_actions" to "Quick actions",
            "new_proposal" to "New proposal",
            "add_customer" to "Add party",
            "recent_invoices" to "Recent Invoices",
            "all_invoices" to "All Invoices",
            "view_all" to "View All",
            "search_hint" to "Search customer, invoice #, phone...",
            "more_title" to "More & Tools",
            "business_profile" to "Business Profile & Letterhead",
            "business_profile_sub" to "Manage GST, Address, UPI, Signatory & Logo",
            "edit_profile" to "Edit Profile",
            "ca_export" to "CSV & Tally XML Export (CA Data)",
            "ca_export_sub" to "1-Tap export for Invoices, Customers, Ledger & Tally Sales Vouchers",
            "customers_title" to "Parties & Customers Directory",
            "customers_sub" to "Manage khata ledger balances, statements & 1-tap WhatsApp",
            "thermal_printer" to "Bluetooth Thermal Printer (ESC/POS)",
            "thermal_printer_sub" to "Connect 58mm / 80mm wireless receipt printers with raw bytes",
            "drive_sync" to "Google Drive Cloud Sync & Backup",
            "drive_sync_sub" to "Zero-cost nightly WorkManager sync, JSON database snapshot & restore",
            "invoice_defaults" to "Invoice Defaults & Numbering",
            "invoice_defaults_sub" to "Prefix (INV-2026-), auto-increment, terms & payment mandate",
            "pro_plan" to "Pro Plan & Testing Mode",
            "pro_plan_sub" to "Multi-firm licensing (100% unlocked in Developer Testing Mode)",
            "system_settings" to "System Settings & Language",
            "system_settings_sub" to "Multi-language support, financial analytics & diagnostics",
            "language_select" to "App Language / भाषा निवडा",
            "language_select_sub" to "Choose your preferred language for the whole app",
            "google_signin_title" to "Google Sign-In & Cloud Sync",
            "google_signin_sub" to "Sign in to backup invoices & sync data securely to Google Drive",
            "continue_google" to "Continue with Google",
            "dev_login_btn" to "⚡ 1-Tap Login as pratik989095@gmail.com",
            "connected_google" to "✓ CONNECTED TO GOOGLE",
            "sign_out" to "Sign Out",
            "sync_now" to "Sync with Google Drive Now",
            "save" to "Save",
            "cancel" to "Cancel",
            "close" to "Close",
            "welcome_title" to "Welcome to BillBook",
            "welcome_subtitle" to "Fast, offline-first billing & khata management suite",
            "choose_lang" to "Select Language / भाषा निवडा",
            "get_started" to "Get Started",
            "skip" to "Skip & Continue Offline",
            "settings_title" to "Settings & Configuration",
            "active_firm" to "Active Business Profile",
            "switch_firm" to "Switch Business",
            "edit_firm" to "Edit Firm & Letterhead",
            "financial_analytics" to "Financial Analytics & Reports",
            "financial_analytics_sub" to "Monthly revenue charts, receivables & tax breakdown",
            "about_app" to "About BillBook",
            "about_app_sub" to "v2.5 CA & GST Compliant • 100% Offline-First Architecture",
            "hardware_sync" to "Hardware & Cloud Sync",
            "acct_export" to "Accounting & CA Data Exports",
            "system_info" to "System Info"
        ),
        "mr" to mapOf(
            "app_name" to "BillBook",
            "nav_home" to "मुख्यपृष्ठ",
            "nav_docs" to "बिले",
            "nav_more" to "अधिक",
            "nav_settings" to "सेटिंग्ज",
            "greeting_title" to "नमस्कार",
            "greeting_sub" to "लाइव्ह बिलिंग आणि खातेवही",
            "dev_mode_badge" to "🧪 डेव्हलपर मोड: अनलॉक",
            "collected_month" to "या महिन्यातील जमा रक्कम",
            "pending_receivables" to "बाकी येणे रक्कम",
            "pending_dues" to "बाकी येणे",
            "lifetime_billed" to "एकूण बिलिंग",
            "open_proposals" to "प्रलंबित अंदाज",
            "create_invoice" to "नवीन टॅक्स बिल तयार करा",
            "record_payment" to "जमा रक्कम नोंदवा",
            "quick_actions" to "जलद कृती",
            "new_proposal" to "नवीन अंदाज",
            "add_customer" to "ग्राहक जोडा",
            "recent_invoices" to "अलीकडील बिले",
            "all_invoices" to "सर्व बिले",
            "view_all" to "सर्व पहा",
            "search_hint" to "ग्राहक किंवा बिल क्रमांक शोधा...",
            "more_title" to "अधिक टूल्स आणि पर्याय",
            "business_profile" to "व्यवसाय प्रोफाइल आणि लेटरहेड",
            "business_profile_sub" to "जीएसटी, पत्ता, यूपीआय, सही आणि लोगो व्यवस्थापित करा",
            "edit_profile" to "प्रोफाइल बदला",
            "ca_export" to "सीएसव्ही आणि टॅली एक्सएमएल एक्सपोर्ट (सीए डेटा)",
            "ca_export_sub" to "बिले, ग्राहक, खातेवही आणि टॅली व्हॉउचर एका टॅपमध्ये पाठवा",
            "customers_title" to "ग्राहक आणि व्यापारी खातेवही",
            "customers_sub" to "खाते शिल्लक, स्टेटमेंट आणि 1-टॅप व्हॉट्सअ‍ॅप रिमाइंडर",
            "thermal_printer" to "ब्लूटूथ थर्मल प्रिंटर (ESC/POS)",
            "thermal_printer_sub" to "58mm / 80mm वायरलेस पावती प्रिंटर जोडा",
            "drive_sync" to "गुगल ड्राईव्ह क्लाउड सिंक आणि बॅकअप",
            "drive_sync_sub" to "दररोज रात्री मोफत सिंक, संपूर्ण डेटा बॅकअप व रिस्टोअर",
            "invoice_defaults" to "बिल नंबरिंग आणि अटी",
            "invoice_defaults_sub" to "प्रिफिक्स (INV-2026-), ऑटो नंबरिंग, नियम आणि अटी",
            "pro_plan" to "प्रो प्लॅन आणि चाचणी मोड",
            "pro_plan_sub" to "सर्व वैशिष्ट्ये डेव्हलपर चाचणीसाठी 100% मोफत अनलॉक",
            "system_settings" to "सिस्टम सेटिंग्ज आणि भाषा",
            "system_settings_sub" to "अ‍ॅपची भाषा, आर्थिक अहवाल आणि तपासणी",
            "language_select" to "अ‍ॅपची भाषा निवडा",
            "language_select_sub" to "संपूर्ण अ‍ॅपसाठी तुमची आवडती भाषा निवडा",
            "google_signin_title" to "गुगल साइन-इन आणि क्लाउड सिंक",
            "google_signin_sub" to "बिले आणि खातेवही गुगल ड्राईव्हवर सुरक्षित सेव्ह करा",
            "continue_google" to "गुगल खात्यासह पुढे जा",
            "dev_login_btn" to "⚡ 1-टॅप लॉगिन (pratik989095@gmail.com)",
            "connected_google" to "✓ गुगल कनेक्ट झाले",
            "sign_out" to "साइन आउट",
            "sync_now" to "आत्ता गुगल ड्राईव्हवर सिंक करा",
            "save" to "जतन करा",
            "cancel" to "रद्द करा",
            "close" to "बंद करा",
            "welcome_title" to "BillBook मध्ये आपले स्वागत आहे",
            "welcome_subtitle" to "जलद, सुरक्षित आणि 100% ऑफलाइन बिलिंग व खातेवही",
            "choose_lang" to "कृपया भाषा निवडा",
            "get_started" to "सुरू करा",
            "skip" to "पुढे जा (ऑफलाइन)",
            "settings_title" to "सेटिंग्ज आणि कॉन्फिगरेशन",
            "active_firm" to "सक्रिय व्यवसाय प्रोफाइल",
            "switch_firm" to "व्यवसाय बदला",
            "edit_firm" to "फर्म व लेटरहेड संपादित करा",
            "financial_analytics" to "आर्थिक विश्लेषण आणि अहवाल",
            "financial_analytics_sub" to "मासिक महसूल चार्ट, येणे बाकी आणि जीएसटी कर विश्लेषण",
            "about_app" to "BillBook बद्दल",
            "about_app_sub" to "v2.5 सीए आणि जीएसटी सुसंगत • 100% ऑफलाइन-प्रथम आर्किटेक्चर",
            "hardware_sync" to "हार्डवेअर आणि क्लाउड सिंक",
            "acct_export" to "अकाउंटिंग आणि सीए डेटा एक्सपोर्ट",
            "system_info" to "सिस्टम माहिती"
        ),
        "hi" to mapOf(
            "app_name" to "BillBook",
            "nav_home" to "होम",
            "nav_docs" to "दस्तावेज़",
            "nav_more" to "अधिक",
            "nav_settings" to "सेटिंग्स",
            "greeting_title" to "नमस्ते",
            "greeting_sub" to "लाइव बिलिंग और खाता बही",
            "dev_mode_badge" to "🧪 डेवलपर मोड: अनलॉक",
            "collected_month" to "इस महीने एकत्र की गई राशि",
            "pending_receivables" to "बकाया प्राप्य राशि",
            "pending_dues" to "बकाया राशि",
            "lifetime_billed" to "कुल बिलिंग",
            "open_proposals" to "खुले प्रस्ताव",
            "create_invoice" to "नया टैक्स इनवॉइस बनाएं",
            "record_payment" to "भुगतान दर्ज करें",
            "quick_actions" to "त्वरित कार्य",
            "new_proposal" to "नया प्रस्ताव",
            "add_customer" to "पार्टी जोड़ें",
            "recent_invoices" to "हाल के इनवॉइस",
            "all_invoices" to "सभी इनवॉइस",
            "view_all" to "सभी देखें",
            "search_hint" to "ग्राहक या बिल खोजें...",
            "more_title" to "अधिक और उपकरण",
            "business_profile" to "व्यवसाय प्रोफ़ाइल और लेटरहेड",
            "business_profile_sub" to "जीएसटी, पता, यूपीआई, हस्ताक्षर और लोगो प्रबंधित करें",
            "edit_profile" to "प्रोफ़ाइल संपादित करें",
            "ca_export" to "सीएसवी और टैली एक्सएमएल निर्यात (सीए डेटा)",
            "ca_export_sub" to "इनवॉइस, ग्राहक, खाता बही और टैली वाउचर 1-टैप में निर्यात करें",
            "customers_title" to "पार्टी और ग्राहक निर्देशिका",
            "customers_sub" to "खाता बही शेष, विवरण और 1-टैप व्हाट्सएप रिमाइंडर",
            "thermal_printer" to "ब्लूटूथ थर्मल प्रिंटर (ESC/POS)",
            "thermal_printer_sub" to "58mm / 80mm वायरलेस रसीद प्रिंटर कनेक्ट करें",
            "drive_sync" to "गूगल ड्राइव्ह क्लाउड सिंक और बैकअप",
            "drive_sync_sub" to "दैनिक निःशुल्क नाइट सिंक और पूर्ण डेटा बैकअप",
            "invoice_defaults" to "इनवॉइस क्रमांकन और शर्तें",
            "invoice_defaults_sub" to "उपसर्ग, स्वतः क्रमांकन और भुगतान शर्तें",
            "pro_plan" to "प्रो प्लान और परीक्षण मोड",
            "pro_plan_sub" to "डेवलपर परीक्षण के लिए सभी सुविधाएं 100% अनलॉक",
            "system_settings" to "सिस्टम सेटिंग्स और भाषा",
            "system_settings_sub" to "ऐप भाषा, वित्तीय रिपोर्ट और निदान",
            "language_select" to "ऐप की भाषा चुनें",
            "language_select_sub" to "पूरे ऐप के लिए अपनी पसंदीदा भाषा चुनें",
            "google_signin_title" to "गूगल साइन-इन और क्लाउड सिंक",
            "google_signin_sub" to "इनवॉइस और डेटा को सुरक्षित रूप से बैकअप करने के लिए साइन इन करें",
            "continue_google" to "गूगल के साथ जारी रखें",
            "dev_login_btn" to "⚡ 1-टैप लॉगिन (pratik989095@gmail.com)",
            "connected_google" to "✓ गूगल से जुड़े हुए हैं",
            "sign_out" to "साइन आउट",
            "sync_now" to "अब गूगल ड्राइव पर सिंक करें",
            "save" to "सहेजें",
            "cancel" to "रद्द करें",
            "close" to "बंद करें",
            "welcome_title" to "BillBook में आपका स्वागत है",
            "welcome_subtitle" to "तेज़, सुरक्षित और 100% ऑफ़लाइन बिलिंग व खाता बही",
            "choose_lang" to "कृपया भाषा चुनें",
            "get_started" to "शुरू करें",
            "skip" to "छोड़ें (ऑफ़लाइन)",
            "settings_title" to "सेटिंग्स और विन्यास",
            "active_firm" to "सक्रिय व्यवसाय प्रोफ़ाइल",
            "switch_firm" to "व्यवसाय बदलें",
            "edit_firm" to "फर्म और लेटरहेड संपादित करें",
            "financial_analytics" to "वित्तीय विश्लेषण और रिपोर्ट",
            "financial_analytics_sub" to "मासिक राजस्व चार्ट, प्राप्य और जीएसटी कर विवरण",
            "about_app" to "BillBook के बारे में",
            "about_app_sub" to "v2.5 सीए और जीएसटी अनुरूप • 100% ऑफ़लाइन-प्रथम आर्किटेक्चर",
            "hardware_sync" to "हार्डवेयर और क्लाउड सिंक",
            "acct_export" to "अकाउंटिंग और सीए डेटा निर्यात",
            "system_info" to "सिस्टम जानकारी"
        ),
        "gu" to mapOf(
            "app_name" to "BillBook",
            "nav_home" to "હોમ",
            "nav_docs" to "દસ્તાવેજો",
            "nav_more" to "વધુ",
            "nav_settings" to "સેટિંગ્સ",
            "greeting_title" to "નમસ્તે",
            "greeting_sub" to "લાઈવ બિલિંગ અને ખાતાવહી",
            "dev_mode_badge" to "🧪 ડેવલપર મોડ: અનલૉક",
            "collected_month" to "આ મહિને જમા રકમ",
            "pending_receivables" to "બાકી આવક",
            "pending_dues" to "બાકી રકમ",
            "lifetime_billed" to "કુલ બિલિંગ",
            "open_proposals" to "ઓપન પ્રપોઝલ",
            "create_invoice" to "નવું ટેક્સ ઇનવોઇસ બનાવો",
            "record_payment" to "ચુકવણી નોંધો",
            "quick_actions" to "ઝડપી ક્રિયાઓ",
            "new_proposal" to "નવો પ્રસ્તાવ",
            "add_customer" to "ગ્રાહક ઉમેરો",
            "recent_invoices" to "તાજેતરના બિલો",
            "all_invoices" to "બધા બિલો",
            "view_all" to "બધા જુઓ",
            "search_hint" to "ગ્રાહક અથવા બિલ શોધો...",
            "more_title" to "વધુ અને ટૂલ્સ",
            "business_profile" to "બિઝનેસ પ્રોફાઇલ અને લેટરહેડ",
            "business_profile_sub" to "જીએસટી, સરનામું, યુપીઆઈ, સહી અને લોગો મેનેજ કરો",
            "edit_profile" to "પ્રોફાઇલ સંપાદિત કરો",
            "ca_export" to "સીએસવી અને ટેલી એક્સએમએલ એક્સપોર્ટ (સીએ ડેટા)",
            "ca_export_sub" to "ઇનવોઇસ, ગ્રાહકો, ખાતાવહી અને ટેલી વાઉચર 1-ટેપમાં એક્સપોર્ટ કરો",
            "customers_title" to "ગ્રાહકો અને ખાતાવહી ડિરેક્ટરી",
            "customers_sub" to "ખાતા બાકી, સ્ટેટમેન્ટ અને 1-ટેપ વોટ્સએપ રિમાઇન્ડર",
            "thermal_printer" to "બ્લૂટૂથ થર્મલ પ્રિન્ટર (ESC/POS)",
            "thermal_printer_sub" to "58mm / 80mm વાયરલેસ રસીદ પ્રિન્ટર કનેક્ટ કરો",
            "drive_sync" to "ગૂગલ ડ્રાઇવ ક્લાઉડ સિંક અને બેકઅપ",
            "drive_sync_sub" to "દરરોજ રાત્રે ફ્રી ઓટો સિંક અને સંપૂર્ણ બેકઅપ",
            "invoice_defaults" to "બિલ નંબરિંગ અને શરતો",
            "invoice_defaults_sub" to "પ્રિફિક્સ, ઓટો નંબરિંગ અને નિયમો",
            "pro_plan" to "પ્રો પ્લાન અને ટેસ્ટિંગ મોડ",
            "pro_plan_sub" to "ડેવલપર ટેસ્ટિંગ માટે તમામ સુવિધાઓ 100% અનલૉક",
            "system_settings" to "સિસ્ટમ સેટિંગ્સ અને ભાષા",
            "system_settings_sub" to "ભાષા પસંદગી અને નાણાકીય રિપોર્ટ્સ",
            "language_select" to "એપ ભાષા પસંદ કરો",
            "language_select_sub" to "આખી એપ માટે તમારી મનપસંદ ભાષા પસંદ કરો",
            "google_signin_title" to "ગૂગલ સાઇન-ઇન અને ક્લાઉડ સિંક",
            "google_signin_sub" to "બિલ અને ડેટા સાચવવા ગૂગલ ડ્રાઇવ સાથે જોડો",
            "continue_google" to "ગૂગલ સાથે આગળ વધો",
            "dev_login_btn" to "⚡ 1-ટેપ લૉગિન (pratik989095@gmail.com)",
            "connected_google" to "✓ ગૂગલ કનેક્ટેડ",
            "sign_out" to "સાઇન આઉટ",
            "sync_now" to "હમણાં ગૂગલ ડ્રાઇવ પર સિંક કરો",
            "save" to "સાચવો",
            "cancel" to "રદ કરો",
            "close" to "બંધ કરો",
            "welcome_title" to "BillBook માં આપનું સ્વાગત છે",
            "welcome_subtitle" to "ઝડપી અને 100% ઑફલાઇન બિલિંગ ખાતાવહી",
            "choose_lang" to "કૃપા કરીને ભાષા પસંદ કરો",
            "get_started" to "શરૂ કરો",
            "skip" to "આગળ વધો (ઑફલાઇન)",
            "settings_title" to "સેટિંગ્સ અને કન્ફિગરેશન",
            "active_firm" to "સક્રિય બિઝનેસ પ્રોફાઇલ",
            "switch_firm" to "બિઝનેસ બદલો",
            "edit_firm" to "ફર્મ અને લેટરહેડ સંપાદિત કરો",
            "financial_analytics" to "નાણાકીય વિશ્લેષણ અને અહેવાલ",
            "financial_analytics_sub" to "માસિક આવક ચાર્ટ, બાકી રકમ અને જીએસટી વિશ્લેષણ",
            "about_app" to "BillBook વિશે",
            "about_app_sub" to "v2.5 સીએ અને જીએસટી માન્ય • 100% ઑફલાઇન આર્કિટેક્ચર",
            "hardware_sync" to "હાર્ડવેર અને ક્લાઉડ સિંક",
            "acct_export" to "એકાઉન્ટિંગ અને સીએ ડેટા એક્સપોર્ટ",
            "system_info" to "સિસ્ટમ માહિતી"
        )
    )
}

/**
 * Convenient Composable helper to get localized text anywhere in Compose.
 */
@Composable
fun appString(key: String): String {
    val lang by AppLanguageManager.currentLanguageFlow.collectAsState()
    return AppLanguageManager.get(key, lang)
}
