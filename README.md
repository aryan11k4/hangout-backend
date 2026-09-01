This is backend project for a Group Chat application.

## WebSocket API — Private Messaging (STOMP over WebSocket)

Real-time private messaging uses STOMP over a raw WebSocket connection
(no SockJS). This is separate from the REST API and does **not** appear in
Swagger/OpenAPI — Swagger only documents HTTP endpoints, and STOMP
destinations are not HTTP routes; they only exist inside an already-open
WebSocket connection. Documented manually here instead.

### Connection

```
ws://localhost:8080/ws?token=<JWT>
```

- Plain WebSocket upgrade — **no SockJS fallback**, connect directly with this URL.
- The JWT must be passed as the `token` query parameter, since a raw WebSocket
  handshake cannot carry a custom `Authorization` header the way a normal HTTP
  request can.
- The token is validated server-side during the handshake
  (`JwtHandshakeInterceptor`). An invalid, missing, or expired token causes
  the handshake itself to be rejected before any STOMP frames are exchanged.
- After the WebSocket connects, the client must still send a STOMP `CONNECT`
  frame before subscribing or sending — this is a protocol-level requirement
  of STOMP itself, separate from the JWT auth above.

### Subscribing (receiving messages)

```
Destination: /user/queue/private-messages
```

Subscribe to this destination to receive any private message sent to you,
in real time, for as long as the connection stays open.

- The `/user` prefix is special — it is not a literal destination. The server
  rewrites it per-session, so each authenticated connection only receives
  messages addressed specifically to that user. You do not need to (and
  cannot meaningfully) specify a user ID in the subscribe destination — the
  server already knows who you are from the JWT used at handshake.

**Received message payload** (`PrivateMessageResponseDto`):

```json
{
  "id": "04d7e65d-cdbf-40b0-9b78-2517d807c63d",
  "senderId": "6dd02e25-f721-45bb-8b05-79020184ddb8",
  "receiverId": "894f3f72-e204-4bc2-b366-7b9600ef4ba5",
  "content": "lets do it!!!",
  "messageType": "TEXT",
  "createdAt": "2026-08-23T14:17:02.441Z",
  "edited": false
}
```

Note: the sender also receives a copy of every message they send on this
same destination, so a sender's other open sessions/devices/tabs stay in
sync.

### Sending a message

```
Destination: /app/private-message.send
```

**Request payload** (`PrivateMessageRequestDto`):

```json
{
  "receiverId": "894f3f72-e204-4bc2-b366-7b9600ef4ba5",
  "content": "Hello!"
}
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `receiverId` | UUID | yes | Must be an existing user's id |
| `content` | string | yes | 1–2000 characters |

- The sender is **never** taken from the payload — it is always resolved
  server-side from the authenticated STOMP session's principal, exactly like
  the REST endpoints. There is no `senderId` field in the request.
- On success, the message is persisted and pushed live to both the receiver
  and the sender's `/user/queue/private-messages` — there is no direct
  request/response frame returned for a `SEND`; the confirmation *is* the
  message arriving back on your own subscribed queue.
- There is currently no explicit error frame sent back to the client on
  failure (e.g. invalid receiver ID) beyond what the server logs — validate
  `receiverId` client-side against a known contact before sending.

### Example client flow (JavaScript, using `stomp.js`)

```javascript
const socket = new WebSocket(`ws://localhost:8080/ws?token=${jwt}`);
const client = Stomp.over(socket);

client.connect({}, () => {
  client.subscribe('/user/queue/private-messages', (frame) => {
    const message = JSON.parse(frame.body);
    console.log('Received:', message);
  });

  client.send('/app/private-message.send', {}, JSON.stringify({
    receiverId: '894f3f72-e204-4bc2-b366-7b9600ef4ba5',
    content: 'Hello!'
  }));
});
```

### Related REST endpoints (documented in Swagger)

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/private-messages/{otherUserId}` | Paginated conversation history with a specific user |
| `POST` | `/api/private-messages` | Persists a message via REST (no live push — use the WebSocket path above for real-time delivery) |