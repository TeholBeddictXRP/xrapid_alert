package space.xrapid.domain;

import lombok.*;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity(name = "EXCHANGE_PAYMENT")
public class ExchangeToExchangePayment extends Payment {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private OffsetDateTime dateTime;
    private Long timestamp;

    @Enumerated(EnumType.STRING)
    private Exchange source;

    private String sourceAddress;
    private String destinationAddress;

    @Enumerated(EnumType.STRING)
    private Exchange destination;
    private Double amount;
    private boolean confirmed;
    private SpottedAt spottedAt;

    @Column(unique = true)
    private String transactionHash;

    @Transient
    private List<Trade> xrpToFiatTrades;

    @Transient
    private List<String> xrpToFiatTradeIds;

    @Transient
    private List<Trade> fiatToXrpTrades;

    @Transient
    private List<String> fiatToXrpTradeIds;

    private double usdValue;

    @Column(length = 500)
    private String tradeIds;

    @Column(length = 500)
    private String tradeOutIds;

    @Enumerated(EnumType.STRING)
    private Currency sourceFiat;

    @Enumerated(EnumType.STRING)
    private Currency destinationFiat;

    private Long tag;

    @Enumerated(EnumType.STRING)
    private Currency destinationCurrencry;

    private boolean inTradeFound = false;

    private boolean outTradeFound = false;

    public String getDateAsString() {
        return dateFormat.format(timestamp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time : ").append(dateFormat.format(timestamp)).append(", ");
        sb.append("Amount : ").append(this.amount).append(", ");
        sb.append("Exchange Source : ").append(this.source == null ? "UNKNOWN" : this.source).append(", ");
        sb.append("Exchange Destination : ").append(this.destination == null ? "UNKNOWN" : this.destination).append(", ");
        sb.append("Address Source : ").append(sourceAddress).append(", ");
        sb.append("Destination : ").append(this.destinationAddress).append(", ");
        sb.append("Destination Tag : ").append(this.tag).append(", ");
        sb.append("Trx Hash : ").append(this.transactionHash);

        return sb.toString();
    }
}
