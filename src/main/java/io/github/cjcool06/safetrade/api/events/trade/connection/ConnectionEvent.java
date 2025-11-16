package io.github.cjcool06.safetrade.api.events.trade.connection;

import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.Event;

public abstract class ConnectionEvent extends Event {

    protected final Side side;

    public ConnectionEvent(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }
}
