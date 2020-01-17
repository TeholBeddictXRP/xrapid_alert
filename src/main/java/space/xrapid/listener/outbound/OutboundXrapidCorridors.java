package space.xrapid.listener.outbound;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import space.xrapid.domain.Exchange;
import space.xrapid.domain.ExchangeToExchangePayment;
import space.xrapid.domain.SpottedAt;
import space.xrapid.domain.Trade;
import space.xrapid.domain.ripple.Payment;
import space.xrapid.listener.XrapidCorridors;
import space.xrapid.service.ExchangeToExchangePaymentService;

import java.util.Comparator;
import java.util.List;

@Slf4j
public class OutboundXrapidCorridors extends XrapidCorridors {

    private Exchange destinationExchange;

    public OutboundXrapidCorridors(ExchangeToExchangePaymentService exchangeToExchangePaymentService, SimpMessageSendingOperations messagingTemplate, Exchange destinationExchange, List<Exchange> exchangesWithApi, MultiKeyMap<String, List<Trade>> tradesCache) {
        super(exchangeToExchangePaymentService, null, messagingTemplate, exchangesWithApi, null, tradesCache);
        this.destinationExchange = destinationExchange;
    }

    @Async
    public void searchXrapidPayments(List<Payment> payments, List<Trade> trades, double rate) {
        this.rate = rate;
        this.trades = trades;

        submit(payments);
    }

    @Override
    protected void submit(List<Payment> payments) {

        if (payments.isEmpty()) {
            return;
        }

        payments.stream()
                .map(this::mapPayment)
                .filter(this::fiatToXrpTradesExists)
                .sorted(Comparator.comparing(ExchangeToExchangePayment::getDateTime))
                .forEach(this::persistPayment);
    }

    @Override
    public Exchange getDestinationExchange() {
        return destinationExchange;
    }

    @Override
    public SpottedAt getSpottedAt() {
        return SpottedAt.SOURCE;
    }
}
