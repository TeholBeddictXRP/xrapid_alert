package space.xrapid.listener.endtoend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import space.xrapid.domain.*;
import space.xrapid.domain.ripple.Payment;
import space.xrapid.listener.XrapidCorridors;
import space.xrapid.service.ExchangeToExchangePaymentService;
import space.xrapid.service.XrapidInboundAddressService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class EndToEndXrapidCorridors extends XrapidCorridors {

    private XrapidInboundAddressService xrapidInboundAddressService;

    private Exchange destinationExchange;

    private Currency sourceFiat;

    public Exchange getDestinationExchange() {
        return destinationExchange;
    }

    public Currency getSourceFiat() {
        return sourceFiat;
    }

    public EndToEndXrapidCorridors(ExchangeToExchangePaymentService exchangeToExchangePaymentService, XrapidInboundAddressService xrapidInboundAddressService, SimpMessageSendingOperations messagingTemplate, Exchange destinationExchange, Currency sourceFiat, long buyDelta, long sellDelta) {

        super(exchangeToExchangePaymentService, messagingTemplate, null);

        this.xrapidInboundAddressService = xrapidInboundAddressService;

        this.buyDelta = buyDelta;
        this.sellDelta = sellDelta;

        this.sourceFiat = sourceFiat;
        this.destinationExchange = destinationExchange;
    }

    public void searchXrapidPayments(List<Payment> payments, List<Trade> trades, double rate) {
        this.rate = rate;

        tradesIdAlreadyProcessed = new HashSet<>();

        this.trades = trades;
        submit(payments);
    }

    @Override
    public SpottedAt getSpottedAt() {
        return SpottedAt.SOURCE_AND_DESTINATION;
    }

    @Override
    protected void submit(List<Payment> payments) {
        List<Payment> paymentsToProcess = payments.stream()
                .filter(this::isXrapidCandidate).collect(Collectors.toList());

        if (paymentsToProcess.isEmpty()) {
            return;
        }

        paymentsToProcess.stream()
                .map(this::mapPayment)
                .filter(this::fiatToXrpTradesExists)
                .filter(this::xrpToFiatTradesExists)
                .sorted(Comparator.comparing(ExchangeToExchangePayment::getDateTime))
                .peek(payment -> persistPayment(payment, true));

        paymentsToProcess.stream()
                .map(this::mapPayment)
                .filter(xrapidInboundAddressService::isXrapidDestination)
                .sorted(Comparator.comparing(ExchangeToExchangePayment::getDateTime))
                .peek(payment -> persistPayment(payment, false));
    }

    @Override
    protected ExchangeToExchangePayment mapPayment(Payment payment) {
        try {
            Exchange source = Exchange.byAddress(payment.getSource());
            Exchange destination = Exchange.byAddress(payment.getDestination());
            boolean xrapidCorridorConfirmed = source.isConfirmed() && destination.isConfirmed();

            OffsetDateTime dateTime = OffsetDateTime.parse(payment.getExecutedTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            return ExchangeToExchangePayment.builder()
                    .amount(Double.valueOf(payment.getDeliveredAmount()))
                    .destination(Exchange.byAddress(payment.getDestination()))
                    .source(Exchange.byAddress(payment.getSource(), getSourceFiat()))
                    .sourceAddress(payment.getSource())
                    .destinationAddress(payment.getDestination())
                    .tag(payment.getDestinationTag())
                    .transactionHash(payment.getTxHash())
                    .timestamp(dateTime.toEpochSecond() * 1000)
                    .dateTime(dateTime)
                    .confirmed(xrapidCorridorConfirmed)
                    .spottedAt(getSpottedAt())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    protected void persistPayment(ExchangeToExchangePayment exchangeToFiatPayment, boolean includetag) {
        if (includetag) {
            xrapidInboundAddressService.add(exchangeToFiatPayment);
        }
       super.persistPayment(exchangeToFiatPayment);
    }
}
