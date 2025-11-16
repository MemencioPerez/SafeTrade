package io.github.cjcool06.safetrade.api.events.trade;

import io.github.cjcool06.safetrade.obj.Trade;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TradeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Trade trade;

    public Trade getTrade() {
        return trade;
    }

    private TradeEvent(Trade trade) {
        this.trade = trade;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Posted before the {@link Trade} is executed.
     */
    public static class Executing extends TradeEvent implements Cancellable {
        private boolean cancel = false;

        public Executing(Trade trade) {
            super(trade);
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

    public static class Executed extends TradeEvent {
        public final Trade.Result tradeResult;

        private Executed(Trade.Result tradeResult) {
            super(tradeResult.getTrade());
            this.tradeResult = tradeResult;
        }

        /**
         * Posted after the {@link Trade} is executed and was successful.
         */
        public static class Success extends Executed {
            public Success(Trade.Result tradeResult) {
                super(tradeResult);
            }
        }

        /**
         * Posted after the {@link Trade} is executed and was unsuccessful.
         */
        public static class Fail extends Executed {
            public Fail(Trade.Result tradeResult) {
                super(tradeResult);
            }
        }
    }

    /**
     * Posted after the {@link Trade} is cancelled.
     */
    public static class Cancelled extends TradeEvent {
        public final Trade.Result tradeResult;

        public Cancelled(Trade.Result tradeResult) {
            super(tradeResult.getTrade());
            this.tradeResult = tradeResult;
        }
    }
}
