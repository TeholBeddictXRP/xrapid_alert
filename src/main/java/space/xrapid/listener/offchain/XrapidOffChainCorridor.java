package space.xrapid.listener.offchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import space.xrapid.domain.Exchange;
import space.xrapid.domain.SpottedAt;
import space.xrapid.domain.Trade;
import space.xrapid.listener.XrapidCorridors;
import space.xrapid.service.ExchangeToExchangePaymentService;
import space.xrapid.service.XrapidInboundAddressService;
import space.xrapid.util.TradesCombinaisonsHelper;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class XrapidOffChainCorridor extends XrapidCorridors {

    private Exchange source;
    private Exchange destination;

    public XrapidOffChainCorridor(ExchangeToExchangePaymentService exchangeToExchangePaymentService, XrapidInboundAddressService xrapidInboundAddressService, SimpMessageSendingOperations messagingTemplate, List<Exchange> exchangesToExclude, Set<String> usedTradeIds) {
        super(exchangeToExchangePaymentService, xrapidInboundAddressService, messagingTemplate, exchangesToExclude, usedTradeIds);
    }

    public XrapidOffChainCorridor(ExchangeToExchangePaymentService exchangeToExchangePaymentService, XrapidInboundAddressService xrapidInboundAddressService, SimpMessageSendingOperations messagingTemplate, List<Trade> trades, Exchange source, Exchange destination, double rate) {
        super();
        this.exchangeToExchangePaymentService = exchangeToExchangePaymentService;
        this.xrapidInboundAddressService = xrapidInboundAddressService;
        this.messagingTemplate = messagingTemplate;
        this.source = source;
        this.trades = trades;
        this.rate = rate;
        this.destination = destination;
    }

    public void search() {
        Map<Double, List<Trade>> buyMap = TradesCombinaisonsHelper.findTradeGroups(trades.stream().filter(trade -> source.equals(trade.getExchange())).collect(Collectors.toList()), "buy");
        Map<Double, List<Trade>> sellMap = TradesCombinaisonsHelper.findTradeGroups(trades.stream().filter(trade -> destination.equals(trade.getExchange())).collect(Collectors.toList()), "sell");


        for (Map.Entry<Double, List<Trade>> buy : buyMap.entrySet()) {
            for (Map.Entry<Double, List<Trade>> sell : sellMap.entrySet()) {
                double diff = buy.getKey() - sell.getKey();
                if (diff == 0 && isBefore(buy.getValue(), sell.getValue())) {

                    log.info("Buy : {}", buy.getValue());
                    log.info("Sell : {}", sell.getValue());
                    log.info("-----------------------------------------");
                }
            }
        }



        System.out.println("FIN !!!!");


    }


    private boolean isBefore(List<Trade> left, List<Trade> right) {

        long buyTime = left.stream().mapToLong(Trade::getTimestamp).max().getAsLong();
        long sellTime = right.stream().mapToLong(Trade::getTimestamp).min().getAsLong();
        return sellTime > buyTime;
    }


    @Override
    public Exchange getDestinationExchange() {
        return null;
    }

    @Override
    public SpottedAt getSpottedAt() {
        return SpottedAt.OFF_CHAIN;
    }
}
