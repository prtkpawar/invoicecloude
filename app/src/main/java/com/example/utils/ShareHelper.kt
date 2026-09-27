package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DocWithDetails
import java.io.File
import java.net.URLEncoder

object ShareHelper {

    fun copyToClipboard(context: Context, text: String, label: String = "Swami Solar Message") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun sharePdf(context: Context, pdfFile: File, text: String = "", targetWhatsAppDirectly: Boolean = false) {
        try {
            if (text.isNotBlank()) {
                copyToClipboard(context, text)
                android.widget.Toast.makeText(
                    context,
                    "📋 Message copied to clipboard! (Paste in WhatsApp chat)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (text.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, text)
                    putExtra(Intent.EXTRA_SUBJECT, text)
                    putExtra("android.intent.extra.TITLE", text)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (targetWhatsAppDirectly) {
                val waPackage = when {
                    isPackageInstalled(context, "com.whatsapp") -> "com.whatsapp"
                    isPackageInstalled(context, "com.whatsapp.w4b") -> "com.whatsapp.w4b"
                    else -> null
                }
                if (waPackage != null) {
                    intent.setPackage(waPackage)
                    context.startActivity(intent)
                    return
                }
            }

            val chooser = Intent.createChooser(intent, "Share PDF / WhatsApp").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Unable to share PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun viewPdf(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openWhatsAppChat(context: Context, mobile: String, message: String): Boolean {
        return try {
            val waNum = Formatters.waNumber(mobile)
            if (waNum.length < 12) return false
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://wa.me/$waNum?text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 1-Tap Payment Reminder — opens WhatsApp directly with pre-filled message.
     * No dialog, no template picker, just instant send.
     */
    fun sendQuickPaymentReminder(
        context: Context,
        customerName: String,
        customerMobile: String,
        docNo: String,
        totalAmount: Double,
        pendingAmount: Double,
        dueDate: String,
        upiVpa: String?,
        firmName: String
    ): Boolean {
        if (customerMobile.isBlank()) return false
        
        val dueDateStr = if (dueDate.isNotBlank()) "\n📅 Due Date: $dueDate" else ""
        val upiStr = if (!upiVpa.isNullOrBlank()) "\n\n💳 Pay via UPI: $upiVpa" else ""
        
        val message = """
Namaskar ${customerName} ji 🙏
This is a gentle reminder regarding your pending payment:
📄 Invoice: $docNo
💰 Total Amount: ₹${Formatters.plain(totalAmount)}
✅ Paid: ₹${Formatters.plain(totalAmount - pendingAmount)}
⏳ Pending: ₹${Formatters.plain(pendingAmount)}$dueDateStr$upiStr

Kindly arrange the payment at your earliest convenience.
Thank you,
$firmName
        """.trimIndent()
        
        return openWhatsAppChat(context, customerMobile, message)
    }

    fun dialPhone(context: Context, mobile: String) {
        try {
            val digits = mobile.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareText(context: Context, text: String) {
        try {
            copyToClipboard(context, text)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Analytics via WhatsApp / App"))
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Unable to share text: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // ENGLISH POLITE & PROFESSIONAL TEMPLATES
    // ==========================================
    val DEFAULT_MSG_ESTIMATE_EN = """
*SWAMI SOLAR* ☀️
_“A SWAMI CONSTRUCTION FIRM”_
━━━━━━━━━━━━━━━━━━━━━
Respected *{customer}*, Namaskar 🙏

Thank you for your interest in Swami Solar rooftop solutions. We are pleased to share the custom quotation prepared for your premises:

📄 *Estimate No:* {docno}
⚡ *System Capacity:* {kw} kW Rooftop Solar
💰 *Estimated Amount:* {total}
📅 *Quotation Validity:* Up to {due}

The detailed techno-commercial estimate PDF is attached above.

Please review the proposal at your convenience. If you need any technical adjustments, subsidy guidance, or have any questions, our team is always happy to assist.

Warm regards,
*SWAMI CONSTRUCTION (SWAMI SOLAR)*
👤 Contact: Mr. Rohit Tatar
📧 Email: solarswamiconstruction@gmail.com
📍 Brahman Galli, Sharda Typing, Pimpalner, Dhule, Maharashtra - 424306

━━━━━━━━━━━━━━━━━━━━━
📌 _Note: This is an automated quotation notification from Swami Solar. For any queries or clarifications, please feel free to reply directly on this chat._
    """.trimIndent()

    val DEFAULT_MSG_INVOICE_EN = """
*SWAMI SOLAR* ☀️
_“A SWAMI CONSTRUCTION FIRM”_
━━━━━━━━━━━━━━━━━━━━━
Respected *{customer}*, Namaskar 🙏

Thank you for choosing Swami Solar (Swami Construction) for your rooftop solar installation. We truly appreciate your trust.

Please find the official GST Tax Invoice details below:

📄 *Invoice No:* {docno}
⚡ *System Capacity:* {kw} kW Rooftop Solar
💰 *Invoice Total:* {total}
✅ *Amount Received:* {paid}
⏳ *Balance Payable:* {pending}

The official signed PDF tax invoice with complete bank details is attached above. Please share the payment reference once done so we can update your account ledger.

Warm regards,
*SWAMI CONSTRUCTION (SWAMI SOLAR)*
👤 Contact: Mr. Rohit Tatar
📧 Email: solarswamiconstruction@gmail.com
📍 Brahman Galli, Sharda Typing, Pimpalner, Dhule, Maharashtra - 424306

━━━━━━━━━━━━━━━━━━━━━
📌 _Note: This is an automated invoice notification. For any billing queries or assistance, please contact our office directly._
    """.trimIndent()

    val DEFAULT_MSG_REMINDER_EN = """
*SWAMI SOLAR* ☀️
_“A SWAMI CONSTRUCTION FIRM”_
━━━━━━━━━━━━━━━━━━━━━
Respected *{customer}*, सस्नेह नमस्कार 🙏

We hope you and your family are enjoying uninterrupted clean solar energy.

This is a gentle and polite follow-up regarding the outstanding balance for your solar project:

📄 *Invoice No:* {docno}
💰 *Total Amount:* {total}
✅ *Received So Far:* {paid}
⏳ *Pending Balance:* {pending}
📅 *Due Date:* {due}

Kindly arrange the payment at your earliest convenience. If you have already completed the payment, please accept our thanks and ignore this message—kindly share the payment receipt/UTR so we can update your account immediately.

Thank you for your cooperation and continued trust.

Warm regards,
*SWAMI CONSTRUCTION (SWAMI SOLAR)*
👤 Contact: Mr. Rohit Tatar
📧 Email: solarswamiconstruction@gmail.com
📍 Brahman Galli, Sharda Typing, Pimpalner, Dhule, Maharashtra - 424306

━━━━━━━━━━━━━━━━━━━━━
📌 _Note: This is an automated reminder from Swami Solar. For any accounting queries or help, please feel free to reach out anytime._
    """.trimIndent()

    val DEFAULT_MSG_RECEIPT_EN = """
*SWAMI SOLAR* ☀️
_“A SWAMI CONSTRUCTION FIRM”_
━━━━━━━━━━━━━━━━━━━━━
Respected *{customer}*, Namaskar 🙏

We gratefully acknowledge receipt of your payment. Thank you very much!

💵 *Amount Received:* {amount}
📄 *Against Invoice:* {docno}
⏳ *Remaining Balance:* {pending}

The official payment receipt is attached as a PDF above for your financial records.

Warm regards,
*SWAMI CONSTRUCTION (SWAMI SOLAR)*
👤 Contact: Mr. Rohit Tatar
📧 Email: solarswamiconstruction@gmail.com
📍 Brahman Galli, Sharda Typing, Pimpalner, Dhule, Maharashtra - 424306

━━━━━━━━━━━━━━━━━━━━━
📌 _Note: This is an automated system-generated payment receipt from Swami Solar._
    """.trimIndent()

    // ==========================================
    // MARATHI (मराठी) POLITE & RESPECTFUL TEMPLATES
    // ==========================================
    val DEFAULT_MSG_ESTIMATE_MR = """
*स्वामी सोलर* ☀️
_“स्वामी कन्स्ट्रक्शन”_
━━━━━━━━━━━━━━━━━━━━━
सस्नेह नमस्कार *{customer}* जी 🙏

स्वामी सोलरवर विश्वास दाखवल्याबद्दल मनःपूर्वक धन्यवाद! आपल्या जागेसाठी तयार केलेले सोलर सिस्टीमचे अधिकृत कोटेशन खालीलप्रमाणे आहे:

📄 *अंदाजपत्रक क्र. (Estimate No):* {docno}
⚡ *सोलर क्षमता:* {kw} kW रूफटॉप सोलर
💰 *एकूण अंदाज रक्कम:* {total}
📅 *वैधता दिनांक:* {due} पर्यंत

सविस्तर तांत्रिक व आर्थिक माहितीचे कोटेशन PDF फाईल स्वरूपात सोबत जोडले आहे. कृपया ते तपासून पाहावे. यामध्ये काही बदल, सबसिडी किंवा इतर शंका असल्यास कृपया आम्हाला निःसंकोचपणे कळवा.

आपला नम्र,
*स्वामी कन्स्ट्रक्शन (स्वामी सोलर)*
👤 संपर्क: श्री. रोहित तातार
📧 ईमेल: solarswamiconstruction@gmail.com
📍 ब्राह्मण गल्ली, शारदा टायपिंग, पिंपळनेर, धुळे, महाराष्ट्र - ४२४३०६

━━━━━━━━━━━━━━━━━━━━━
📌 _टीप: हा संदेश सिस्टीमद्वारे स्वयंचलित पाठवला गेला आहे. कोणत्याही चौकशी किंवा माहितीसाठी कृपया या चॅटवर उत्तर द्या._
    """.trimIndent()

    val DEFAULT_MSG_INVOICE_MR = """
*स्वामी सोलर* ☀️
_“स्वामी कन्स्ट्रक्शन”_
━━━━━━━━━━━━━━━━━━━━━
सस्नेह नमस्कार *{customer}* जी 🙏

आपल्या सोलर रूफटॉप प्रकल्पासाठी स्वामी सोलरची (स्वामी कन्स्ट्रक्शन) निवड केल्याबद्दल धन्यवाद. आपले अधिकृत GST टॅक्स इनव्हॉईस खालीलप्रमाणे आहे:

📄 *बिल क्र. (Invoice No):* {docno}
⚡ *सिस्टीम क्षमता:* {kw} kW सोलर
💰 *एकूण बिल रक्कम:* {total}
✅ *जमा रक्कम:* {paid}
⏳ *शिल्लक रक्कम (Pending):* {pending}

अधिकृत बिल PDF सोबत जोडली आहे. बँकेचे डिटेल्स बिलावर नमूद आहेत. पेमेंट झाल्यावर कृपया ट्रान्झॅक्शन संदर्भ शेअर करावा ही विनंती.

आपला नम्र,
*स्वामी कन्स्ट्रक्शन (स्वामी सोलर)*
👤 संपर्क: श्री. रोहित तातार
📧 ईमेल: solarswamiconstruction@gmail.com
📍 ब्राह्मण गल्ली, शारदा टायपिंग, पिंपळनेर, धुळे, महाराष्ट्र - ४२४३०६

━━━━━━━━━━━━━━━━━━━━━
📌 _टीप: हा संदेश सिस्टीमद्वारे स्वयंचलित पाठवला गेला आहे. बिलिंग संबंधित कोणत्याही माहितीसाठी कृपया थेट संपर्क साधा._
    """.trimIndent()

    val DEFAULT_MSG_REMINDER_MR = """
*स्वामी सोलर* ☀️
_“स्वामी कन्स्ट्रक्शन”_
━━━━━━━━━━━━━━━━━━━━━
सस्नेह नमस्कार *{customer}* जी 🙏

आशा आहे की आपले कुटुंब आणि व्यवसाय उत्तम सुरू असेल. आपल्या सोलर सिस्टीमच्या उर्वरित देय बिलाच्या संदर्भात ही एक नम्र आठवण:

📄 *बिल क्र. (Invoice No):* {docno}
💰 *एकूण रक्कम:* {total}
✅ *आतापर्यंत जमा:* {paid}
⏳ *शिल्लक देय रक्कम:* {pending}
📅 *देय दिनांक (Due Date):* {due}

कृपया आपल्या सोयीनुसार लवकरात लवकर शिल्लक रक्कम वर्ग करण्याची व्यवस्था करावी ही नम्र विनंती. आपण पेमेंट आधीच केले असल्यास या संदेशाकडे दुर्लक्ष करावे आणि पेमेंट पावती/स्क्रीनशॉट शेअर करावा जेणेकरून आम्ही आपले खाते तात्काळ अपडेट करू शकू.

आपल्या सहकार्याबद्दल मनापासून धन्यवाद!

आपला नम्र,
*स्वामी कन्स्ट्रक्शन (स्वामी सोलर)*
👤 संपर्क: श्री. रोहित तातार
📧 ईमेल: solarswamiconstruction@gmail.com
📍 ब्राह्मण गल्ली, शारदा टायपिंग, पिंपळनेर, धुळे, महाराष्ट्र - ४२४३०६

━━━━━━━━━━━━━━━━━━━━━
📌 _टीप: हा संदेश स्वामी सोलर प्रणालीद्वारे पाठवला गेला आहे. कोणत्याही शंका किंवा मदतीसाठी कृपया आमच्याशी थेट संपर्क साधा._
    """.trimIndent()

    val DEFAULT_MSG_RECEIPT_MR = """
*स्वामी सोलर* ☀️
_“स्वामी कन्स्ट्रक्शन”_
━━━━━━━━━━━━━━━━━━━━━
सस्नेह नमस्कार *{customer}* जी 🙏

आपल्याकडून पेमेंट यशस्वीरित्या प्राप्त झाले आहे. त्याबद्दल आपले खूप खूप धन्यवाद!

💵 *प्राप्त रक्कम:* {amount}
📄 *बिल क्रमांक:* {docno}
⏳ *उर्वरित शिल्लक:* {pending}

अधिकृत डिजिटल पावतीची PDF सोबत जोडली आहे.

आपला नम्र,
*स्वामी कन्स्ट्रक्शन (स्वामी सोलर)*
👤 संपर्क: श्री. रोहित तातार
📧 ईमेल: solarswamiconstruction@gmail.com
📍 ब्राह्मण गल्ली, शारदा टायपिंग, पिंपळनेर, धुळे, महाराष्ट्र - ४२४३०६

━━━━━━━━━━━━━━━━━━━━━
📌 _टीप: हा संदेश अधिकृत पेमेंट पावती पुष्टीकरणासाठी स्वामी सोलर प्रणालीद्वारे पाठवला आहे._
    """.trimIndent()

    // Aliases for backwards compatibility
    val DEFAULT_MSG_ESTIMATE = DEFAULT_MSG_ESTIMATE_EN
    val DEFAULT_MSG_INVOICE = DEFAULT_MSG_INVOICE_EN
    val DEFAULT_MSG_REMINDER = DEFAULT_MSG_REMINDER_EN
    val DEFAULT_MSG_RECEIPT = DEFAULT_MSG_RECEIPT_EN

    enum class MessageLanguage(val displayName: String, val flag: String) {
        ENGLISH("English", "🇬🇧"),
        MARATHI("मराठी", "🇮🇳")
    }

    enum class TemplateType(val title: String) {
        ESTIMATE("Estimate / Quotation"),
        INVOICE("Tax Invoice"),
        REMINDER("Payment Reminder"),
        RECEIPT("Payment Receipt")
    }

    fun getDefaultTemplate(type: TemplateType, language: MessageLanguage): String {
        return when (type) {
            TemplateType.ESTIMATE -> if (language == MessageLanguage.MARATHI) DEFAULT_MSG_ESTIMATE_MR else DEFAULT_MSG_ESTIMATE_EN
            TemplateType.INVOICE -> if (language == MessageLanguage.MARATHI) DEFAULT_MSG_INVOICE_MR else DEFAULT_MSG_INVOICE_EN
            TemplateType.REMINDER -> if (language == MessageLanguage.MARATHI) DEFAULT_MSG_REMINDER_MR else DEFAULT_MSG_REMINDER_EN
            TemplateType.RECEIPT -> if (language == MessageLanguage.MARATHI) DEFAULT_MSG_RECEIPT_MR else DEFAULT_MSG_RECEIPT_EN
        }
    }

    fun getSettingKey(type: TemplateType, language: MessageLanguage): String {
        val langSuffix = if (language == MessageLanguage.MARATHI) "_mr" else "_en"
        val base = when (type) {
            TemplateType.ESTIMATE -> "msg_estimate"
            TemplateType.INVOICE -> "msg_invoice"
            TemplateType.REMINDER -> "msg_reminder"
            TemplateType.RECEIPT -> "msg_receipt"
        }
        return "$base$langSuffix"
    }

    fun formatTemplateMessage(
        template: String,
        detail: DocWithDetails,
        amount: Double = 0.0
    ): String {
        val doc = detail.doc
        val kwStr = if (doc.kw % 1.0 == 0.0) doc.kw.toInt().toString() else doc.kw.toString()
        val rawDue = if (doc.isInvoice) doc.dueDate else doc.validTill
        val dueStr = if (rawDue.isBlank()) "the agreed date" else Formatters.fmtDate(rawDue)

        return template
            .replace("{customer}", detail.customerName)
            .replace("{docno}", doc.docNo)
            .replace("{kw}", kwStr)
            .replace("{total}", Formatters.money(doc.total))
            .replace("{paid}", Formatters.money(detail.totalPaid))
            .replace("{pending}", Formatters.money(detail.pending))
            .replace("{amount}", Formatters.money(amount))
            .replace("{due}", dueStr)
    }
}
