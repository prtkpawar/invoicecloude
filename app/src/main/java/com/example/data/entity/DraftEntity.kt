package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for auto-saving form drafts.
 *
 * Drafts are:
 * - Created/updated automatically every 400ms while the user edits a form
 * - Loaded on form open to offer "Resume Draft" if a recent draft exists
 * - Deleted on successful document save
 *
 * The [payloadJson] field stores the full form state serialized as JSON,
 * allowing the form to be restored exactly as the user left it.
 *
 * A draft with [docId] = null represents a new, unsaved document.
 * A draft with a non-null [docId] represents edits to an existing document.
 */
@Entity(
    tableName = "drafts",
    indices = [Index(value = ["docId"], unique = false)]
)
data class DraftEntity(
    @PrimaryKey(autoGenerate = true)
    val draftId: Long = 0,

    /** Non-null when editing an existing document */
    val docId: Int? = null,

    /** Document type (ESTIMATE, INVOICE, CONST_ESTIMATE, etc.) */
    val docType: String,

    /** Business profile ID this draft belongs to */
    val businessId: Long = 1L,

    /** JSON-serialized form state (all fields, items, selections) */
    val payloadJson: String,

    /** Timestamp of last modification for "Resume draft from 2:34 PM?" display */
    val lastModified: Long = System.currentTimeMillis(),
)
