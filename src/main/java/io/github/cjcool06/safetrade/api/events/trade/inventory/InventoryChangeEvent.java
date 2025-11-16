package io.github.cjcool06.safetrade.api.events.trade.inventory;

import io.github.cjcool06.safetrade.obj.Side;
import org.bukkit.event.Event;

public abstract class InventoryChangeEvent extends Event {

    protected final Side side;

    public InventoryChangeEvent(Side side) {
        this.side = side;
    }

    public Side getSide() {
        return side;
    }
}
