package org.trinetra.android.core.sync.model

data class SyncOperation(
    val operationId: String,
    val entityType: String,
    val entityId: String,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val payloadJson: String,
    val clientTimestamp: String,
    val version: Int = 1
)

data class SyncPushRequest(
    val operations: List<SyncOperation>
)

data class SyncPushResponse(
    val processedCount: Int,
    val serverSeq: String
)

data class SyncChangeItem(
    val operationId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val serverSeq: Long
)

data class SyncPullResponse(
    val changes: List<SyncChangeItem>,
    val nextCursor: String
)
