package org.trinetra.android.core.sync

import java.util.UUID
import org.trinetra.android.core.sync.model.SyncChangeItem
import org.trinetra.android.core.sync.model.SyncOperation
import org.trinetra.android.core.sync.model.SyncPullResponse
import org.trinetra.android.core.sync.model.SyncPushRequest
import org.trinetra.android.core.sync.model.SyncPushResponse

/**
 * Pure Kotlin offline-first sync engine implementing the outbox pattern (docs/architecture/sync.md).
 * Manages idempotency keys, batch generation, and cursor-based incremental sync.
 */
class SyncEngine {

    private val pendingOutbox = mutableListOf<SyncOperation>()
    private var lastSyncedCursor: Long = 0L

    val pendingCount: Int
        get() = pendingOutbox.size

    val currentCursor: Long
        get() = lastSyncedCursor

    /**
     * Enqueues a local write mutation into the sync outbox with a unique idempotency key.
     */
    fun enqueueMutation(
        entityType: String,
        entityId: String,
        operation: String,
        payloadJson: String,
        clientTimestamp: String,
        version: Int = 1
    ): SyncOperation {
        val op = SyncOperation(
            operationId = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            payloadJson = payloadJson,
            clientTimestamp = clientTimestamp,
            version = version
        )
        pendingOutbox.add(op)
        return op
    }

    /**
     * Prepares an idempotent batch push request.
     */
    fun preparePushRequest(batchSize: Int = 50): SyncPushRequest {
        val batch = pendingOutbox.take(batchSize)
        return SyncPushRequest(operations = batch)
    }

    /**
     * Confirms that a batch was acknowledged by the server up to serverSeq.
     */
    fun acknowledgePush(response: SyncPushResponse, acknowledgedCount: Int) {
        val countToRemove = minOf(acknowledgedCount, pendingOutbox.size)
        repeat(countToRemove) {
            if (pendingOutbox.isNotEmpty()) {
                pendingOutbox.removeAt(0)
            }
        }
        val seqNum = response.serverSeq.toLongOrNull()
        if (seqNum != null && seqNum > lastSyncedCursor) {
            lastSyncedCursor = seqNum
        }
    }

    /**
     * Reconciles incoming remote changes using last-writer-wins for private records.
     */
    fun reconcilePull(response: SyncPullResponse): List<SyncChangeItem> {
        val nextSeq = response.nextCursor.toLongOrNull()
        if (nextSeq != null && nextSeq > lastSyncedCursor) {
            lastSyncedCursor = nextSeq
        }
        return response.changes
    }

    /**
     * Resolves conflict between local and remote record based on version.
     * Returns true if remote record wins and should overwrite local.
     */
    fun shouldRemoteOverwrite(localVersion: Int, remoteVersion: Int): Boolean {
        return remoteVersion >= localVersion
    }
}
