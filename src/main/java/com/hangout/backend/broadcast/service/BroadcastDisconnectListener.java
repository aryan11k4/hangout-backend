package com.hangout.backend.broadcast.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * INTENTIONAL NO-OP as of the latest product decision.
 * <p>
 * Previously, this listener auto-ended any broadcast the disconnecting
 * user was hosting (covering closed tabs, lost connections, crashes).
 * That behavior has been deliberately REMOVED: a host's connection
 * dropping - reload, closed tab, lost network - must no longer end the
 * broadcast. The host can reload, rejoin, and explicitly end it whenever
 * they're ready. Only POST /broadcasts/{id}/end ends a broadcast now.
 * <p>
 * A broadcast whose host never returns to end it is no longer cleaned up
 * by this listener - instead, StaleBroadcastCleanupTask sweeps and
 * auto-ends any broadcast still ACTIVE more than 5 hours after it started,
 * on every application startup. See that class for details.
 * <p>
 * This class is kept in place (rather than deleted) so a future reader
 * sees this was a deliberate removal, not an oversight, and so the
 * SessionDisconnectEvent wiring stays visible/discoverable if disconnect
 * handling is ever needed again for something else.
 */
@Component
public class BroadcastDisconnectListener {

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        // Per product decision: a host's connection dropping (reload,
        // closed tab, lost network) must NOT end the broadcast anymore.
        // The host can reload, rejoin, and end it explicitly themselves
        // whenever they're ready. Only the explicit
        // POST /broadcasts/{id}/end call ends a broadcast now - this
        // listener intentionally does nothing.
    }
}