package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import com.example.ui.theme.pro.Brand600
import com.example.ui.theme.pro.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.BusinessProfile
import com.example.data.model.Company
import com.example.data.model.DocWithDetails
import com.example.ui.theme.SwiggyBorder
import com.example.ui.theme.SwiggyDark
import com.example.ui.theme.SwiggyGray
import com.example.utils.PdfGenerator
import com.example.utils.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewDialog(
    detail: DocWithDetails,
    company: Company?,
    activeFirm: BusinessProfile?,
    initialTheme: String = "MODERN_COLOR",
    onDismiss: () -> Unit,
    onShareWhatsApp: (File, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTheme by remember { mutableStateOf(initialTheme) }
    var renderedBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var currentPdfFile by remember { mutableStateOf<File?>(null) }
    var isRendering by remember { mutableStateOf(true) }
    var renderingError by remember { mutableStateOf<String?>(null) }

    fun renderPdfWithTheme(theme: String) {
        isRendering = true
        renderingError = null
        scope.launch(Dispatchers.IO) {
            try {
                val pdf = PdfGenerator.generatePdf(
                    context = context,
                    detail = detail,
                    company = company ?: com.example.data.model.Company(),
                    business = activeFirm,
                    themeStr = theme
                )
                currentPdfFile = pdf

                val pfd = ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val pageCount = renderer.pageCount
                val bitmaps = mutableListOf<Bitmap>()

                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // 2x scaling for sharp crisp text rendering
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                renderer.close()
                pfd.close()

                withContext(Dispatchers.Main) {
                    renderedBitmaps = bitmaps
                    isRendering = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    renderingError = "Unable to render PDF preview: ${e.localizedMessage}"
                    isRendering = false
                }
            }
        }
    }

    LaunchedEffect(selectedTheme, activeFirm?.id) {
        renderPdfWithTheme(selectedTheme)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar
                Surface(
                    color = Brand600,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Real-Time PDF Preview",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                val firmName = activeFirm?.brandName?.ifBlank { activeFirm.legalName } ?: "Active Firm"
                                Text(
                                    text = "${detail.doc.docNo} • Firm: $firmName",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Active Firm Metadata Summary Bar
                Surface(
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = Brand600, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val logoStatus = if (!activeFirm?.logoPath.isNullOrBlank()) "Logo ✓" else "Monogram Logo"
                            val sigStatus = if (!activeFirm?.signaturePath.isNullOrBlank()) "Signatory Seal ✓" else "Digital Seal"
                            val gstinStr = if (!activeFirm?.gstin.isNullOrBlank()) "GSTIN: ${activeFirm.gstin}" else "No GSTIN"
                            Text(
                                text = "$logoStatus • $sigStatus • $gstinStr",
                                fontSize = 11.5.sp,
                                color = SwiggyDark,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verified Metadata", fontSize = 10.5.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Theme Switcher Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = SwiggyGray, modifier = Modifier.size(16.dp))
                    Text("PDF Style:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SwiggyDark)

                    mapOf(
                        "MODERN_COLOR" to "Modern Corporate",
                        "CLASSIC_BLUE" to "Classic Navy",
                        "MINIMAL_SLATE" to "Minimal Slate",
                        "SOLAR_GOLD" to "Solar Gold / Executive"
                    ).forEach { (themeKey, themeName) ->
                        FilterChip(
                            selected = selectedTheme == themeKey,
                            onClick = { selectedTheme = themeKey },
                            label = { Text(themeName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Brand600,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF8FAFC),
                                labelColor = SwiggyDark
                            )
                        )
                    }
                }

                // PDF Renderer Body View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF64748B))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRendering) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Rendering PDF page canvas...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    } else if (renderingError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Preview Error", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(renderingError!!, fontSize = 12.sp, color = SwiggyDark)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            renderedBitmaps.forEachIndexed { index, bitmap ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        shadowElevation = 6.dp,
                                        color = Color.White
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "PDF Page ${index + 1}",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                    if (renderedBitmaps.size > 1) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Page ${index + 1} of ${renderedBitmaps.size}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Footer
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentPdfFile?.let { file ->
                                    ShareHelper.viewPdf(context, file)
                                } ?: Toast.makeText(context, "Generating PDF...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open System PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                currentPdfFile?.let { file ->
                                    val msg = "Please find attached document ${detail.doc.docNo} from ${activeFirm?.brandName ?: activeFirm?.legalName ?: "our firm"}."
                                    onShareWhatsApp(file, msg)
                                } ?: Toast.makeText(context, "PDF is rendering...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share WhatsApp PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
