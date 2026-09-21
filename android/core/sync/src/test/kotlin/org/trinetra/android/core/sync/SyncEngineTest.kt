package org.trinetra.android.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.trinetra.android.core.sync.model.SyncChangeItem
import org.trinetra.android.core.sync.model.SyncPullResponse
import org.trinetra.android.core.sync.model.SyncPushResponse

class SyncEngineTest {

    @Test
    fun testEnqueueAndPrepareBatch() {
        val engine = SyncEngine()
        assertEquals(0, engine.pendingCount)

        val op1 = engine.enqueueMutation(
            entityType = "observation",
            entityId = "obs-1",
            operation = "INSERT",
            payloadJson = "{\"value\": 5.4}",
            clientTimestamp = "2026-09-22T00:00:00Z"
        )
        val op2 = engine.enqueueMutation(
            entityType = "document",
            entityId = "doc-1",
            operation = "INSERT",
            payloadJson = "{\"sha256\": \"abc\"}",
            clientTimestamp = "2026-09-22T00:01:00Z"
        )

        assertEquals(2, engine.pendingCount)
        assertTrue(op1.operationId.isNotEmpty())
        assertTrue(op2.operationId.isNotEmpty())
        assertTrue(op1.operationId != op2.operationId) // Idempotency keys must be unique

        val batch = engine.preparePushRequest(batchSize = 10)
        assertEquals(2, batch.operations.size)
    }

    @Test
    fun testAcknowledgePush_removesItemsAndUpdatesCursor() {
        val engine = SyncEngine()
        engine.enqueueMutation("observation", "obs-1", "INSERT", "{}", "2026-09-22T00:00:00Z")
        engine.enqueueMutation("observation", "obs-2", "INSERT", "{}", "2026-09-22T00:01:00Z")

        val response = SyncPushResponse(processedCount = 2, serverSeq = "42")
        engine.acknowledgePush(response, acknowledgedCount = 2)

        assertEquals(0, engine.pendingCount)
        assertEquals(42L, engine.currentCursor)
    }

    @Test
    fun testReconcilePull_advancesCursor() {
        val engine = SyncEngine()
        val changes = listOf(
            SyncChangeItem("op-1", "document", "doc-1", "INSERT", "{}", 100L)
        )
        val pullResponse = SyncPullResponse(changes = changes, nextCursor = "105")

        val reconciled = engine.reconcilePull(pullResponse)
        assertEquals(1, reconciled.size)
        assertEquals(105L, engine.currentCursor)
    }

    @Test
    fun testConflictResolution_versionEvaluation() {
        val engine = SyncEngine()
        assertTrue(engine.shouldRemoteOverwrite(localVersion = 1, remoteVersion = 2))
        assertTrue(engine.shouldRemoteOverwrite(localVersion = 2, remoteVersion = 2))
        assertFalse(engine.shouldRemoteOverwrite(localVersion = 3, remoteVersion = 2))
    }
}
