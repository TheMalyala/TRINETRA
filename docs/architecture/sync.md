# Synchronization Protocol (Sync Outbox)

Trinetra implements an **offline-first outbox pattern** with cursor-based synchronization and idempotency keys.

## Client Outbox Flow
1. **Local Transaction**: Every local write that must sync to the backend creates an entry in the `sync_outbox` table within the same Room transaction.
2. **WorkManager Trigger**: A scheduled `SyncWorker` fires upon network connectivity (`NetworkType.CONNECTED`).
3. **Idempotent Push**:
   - Outbox operations are packaged into a batch with unique UUID `operation_id`s.
   - Header `Idempotency-Key: <operation_id>` ensures safe server-side retries.
4. **Server Pull**:
   - The client polls `/api/v1/sync/pull?cursor=<last_server_seq>`.
   - The server responds with changed shared objects (appointments, doctor responses, verified advice).
5. **Conflict Resolution**:
   - **Private Records**: Last-writer-wins with monotonically increasing version numbers; conflicts preserve both records as versions.
   - **Shared Objects**: Server-authoritative timestamps dictate slot bookings and signed advice state.
