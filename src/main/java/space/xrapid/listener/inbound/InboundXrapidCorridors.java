package space.xrapid.listener.inbound;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import space.xrapid.domain.Exchange;
import space.xrapid.domain.SpottedAt;
import space.xrapid.domain.Trade;
import space.xrapid.domain.ripple.Payment;
import space.xrapid.listener.XrapidCorridors;
import space.xrapid.service.ExchangeToExchangePaymentService;

import java.util.HashSet;
import java.util.List;

@Slf4j
public class InboundXrapidCorridors extends XrapidCorridors {

    private Exchange destinationExchange;

    public InboundXrapidCorridors(ExchangeToExchangePaymentService exchangeToExchangePaymentService, SimpMessageSendingOperations messagingTemplate, Exchange destinationExchange, List<Exchange> exchangesWithApi, MultiKeyMap<String, List<Trade>> tradesCache) {
        super(exchangeToExchangePaymentService, null, messagingTemplate, exchangesWithApi, null, tradesCache);
        this.destinationExchange = destinationExchange;
    }

    public void searchXrapidPayments(List<Payment> payments, List<Trade> trades, double rate) {

        this.rate = rate;

        this.trades = trades;

        tradesIdAlreadyProcessed = new HashSet<>();

        submit(payments);
    }

    @Override
    public Exchange getDestinationExchange() {
        return destinationExchange;
    }

    @Override
    public SpottedAt getSpottedAt() {
        return SpottedAt.DESTINATION;
    }
}
