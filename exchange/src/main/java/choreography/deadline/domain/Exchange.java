package choreography.deadline.domain;

import choreography.deadline.ExchangeApplication;
import choreography.deadline.domain.ExchangeFailed;
import choreography.deadline.domain.ExchangeSucceed;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Exchange_table")
@Data
public class Exchange {

    private Long currencyId;

    private Long amount;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;

    private Double rate;

    private Double pointUsed;

    @PostPersist
    public void onPostPersist() {
    }

    public static ExchangeRepository repository() {
        ExchangeRepository exchangeRepository = ExchangeApplication.applicationContext.getBean(
            ExchangeRepository.class
        );
        return exchangeRepository;
    }

    public static void exchange(OrderCreated orderCreated) {

        Exchange exchange = new Exchange();

        //FOCUS: anti-corruption
        exchange.setAmount(orderCreated.getAmount().longValue());
        exchange.setCurrencyId(Long.valueOf(orderCreated.getCurrencyId()));
        exchange.setOrderId(orderCreated.getId());

        // FOCUS: 랜덤하게 환률이 바뀌는 상황
        //exchange.setRate(1.5 * (1 + Math.random()));
        exchange.setRate(1.0); //편의상 point:currency = 1:1 환률

        exchange.setPointUsed(exchange.getAmount() * exchange.getRate());

        repository().save(exchange);

        ExchangeSucceed exchangeSucceed = new ExchangeSucceed(exchange);
        exchangeSucceed.setUserId(orderCreated.getHolderId());  //FOCUS: event enrichment

        exchangeSucceed.publishAfterCommit();

        // FOCUS:
        // if (some business reason, not a technical reason){
        //  ExchangeFailed exchangeFailed = new ExchangeFailed(exchange);
        //  exchangeFailed.publishAfterCommit();
        // }

    }

    // FOCUS: this process must be idempotent. since this could be tried one or more times:
    // this will be finally processed by Kafka event
    public static void compensate(OrderRejected orderRejected) {
        
        repository().findByOrderId(orderRejected.getId()).ifPresent(exchange->{
            
            repository().delete(exchange);

         });

    }
}
