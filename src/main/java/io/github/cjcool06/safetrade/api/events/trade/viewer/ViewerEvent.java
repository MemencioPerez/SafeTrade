package io.github.cjcool06.safetrade.api.events.trade.viewer;

import io.github.cjcool06.safetrade.obj.Trade;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ViewerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    public final Trade trade;
    public final Player viewer;

    private ViewerEvent(Trade trade, Player viewer) {
        this.trade = trade;
        this.viewer = viewer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static class Add extends ViewerEvent {

        private Add(Trade trade, Player player) {
            super(trade, player);
        }

        /**
         * Posted before the viewer is added to the {@link Trade}.
         */
        public static class Pre extends Add implements Cancellable {
            private boolean cancel = false;

            public Pre(Trade trade, Player player) {
                super(trade, player);
            }

            @Override
            public boolean isCancelled() {
                return cancel;
            }

            @Override
            public void setCancelled(boolean cancel) {
                this.cancel = cancel;
            }
        }

        /**
         * Posted after the viewer is added to the {@link Trade}.
         */
        public static class Post extends Add {
            public Post(Trade trade, Player player) {
                super(trade, player);
            }
        }
    }

    public static class Remove extends ViewerEvent {

        private Remove(Trade trade, Player player) {
            super(trade, player);
        }

        /**
         * Posted before the viewer is removed from the {@link Trade}.
         */
        public static class Pre extends Remove implements Cancellable {
            private boolean cancel = false;

            public Pre(Trade trade, Player player) {
                super(trade, player);
            }

            @Override
            public boolean isCancelled() {
                return cancel;
            }

            @Override
            public void setCancelled(boolean cancel) {
                this.cancel = cancel;
            }
        }

        /**
         * Posted after the viewer is removed from the {@link Trade}.
         */
        public static class Post extends Remove {
            public Post(Trade trade, Player player) {
                super(trade, player);
            }
        }
    }
}
