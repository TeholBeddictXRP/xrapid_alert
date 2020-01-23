package space.xrapid.util;

import org.paukov.combinatorics3.Generator;
import org.paukov.combinatorics3.IGenerator;
import space.xrapid.domain.Trade;

import java.util.*;
import java.util.stream.Collectors;

public class TradesCombinaisonsHelper {


    public static List<Trade> getTrades(List<Trade> trades, double amount) {

        int maxSize = trades.size() > 9 ? 9 : trades.size();

        return getTrades(trades, amount, maxSize);
    }

    private static List<Trade> getTrades(List<Trade> trades, double amount, int maxSize) {
        List<Trade> toReturn = new ArrayList<>();

        double min = 10000;


        for (int i = 1; i <= maxSize; i++) {

            long start = new Date().getTime();

            IGenerator<List<Trade>> combinaisons = Generator.combination(trades).simple(i);

            Iterator<List<Trade>> iterator = combinaisons.iterator();

            while (iterator.hasNext()) {
                List<Trade> candidates = iterator.next();

                double sum = sum(candidates);
                double diff = Math.abs(sum - amount);

                if (diff <= 0.05) {

                    if (diff <= 0.02) {
                        return candidates;
                    }

                    if (diff < min) {
                        toReturn =  candidates;
                        min = diff;

                        if (new Date().getTime() - start > 15000) {
                            break;
                        }
                    }
                }

            }
        }

        return toReturn;
    }


    public static Map<Double, List<Trade>> findTradeGroups(List<Trade> trades, String side) {

        Map<Double, List<Trade>> result = new HashMap<>();

        List<Trade> sideTrades = trades.stream().filter(trade -> trade.getSide().equals(side)).collect(Collectors.toList());

        for (int i = 1; i <= 3; i++) {

            IGenerator<List<Trade>> combinaisons = Generator.combination(sideTrades).simple(i);

            Iterator<List<Trade>> iterator = combinaisons.iterator();

            while (iterator.hasNext()) {
                List<Trade> candidates = iterator.next();

                double sum = sum(candidates);

                if (sum > 5000) {
                    result.put(sum, candidates);
                }

            }
        }

        return result;
    }

    public static Double sum(List<Trade> groups) {
        return groups.stream().mapToDouble(Trade::getAmount).sum();
    }

}
