package io.github.cjcool06.safetrade.api.events.trade.transaction.money;

import io.github.cjcool06.safetrade.api.events.trade.transaction.TransactionEvent;
import io.github.cjcool06.safetrade.obj.MoneyWrapper;
import io.github.cjcool06.safetrade.obj.Vault;

public abstract class MoneyTransactionEvent extends TransactionEvent {

    protected final MoneyWrapper wrapper;

    public MoneyTransactionEvent(Vault vault, MoneyWrapper wrapper) {
        super(vault);
        this.wrapper = wrapper;
    }
}
