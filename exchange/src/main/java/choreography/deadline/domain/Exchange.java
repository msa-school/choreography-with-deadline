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

    @Id
    private Long orderId;

    private Long currencyId;

    private Long amount;

    private Double rate;

    private Double pointUsed;

    //@Transient
    private Long delaySeconds;

    @PrePersist
    public void delay() {
        try{
            if(getDelaySeconds()!=null){
                Thread.sleep(getDelaySeconds() * 1000);
            }
        }catch(Exception e){}
    }

    public static ExchangeRepository repository() {
        ExchangeRepository exchangeRepository = ExchangeApplication.applicationContext.getBean(
            ExchangeRepository.class
        );
        return exchangeRepository;
    }

    public static void exchange(OrderCreated orderCreated) {

        ///if(orderCreated.getTimestamp() > new Date)  //FOCUS time out 인것은 처리하지 않도록 하는게 현실적

        Exchange exchange = new Exchange();

        //FOCUS: anti-corruption
        exchange.setAmount(orderCreated.getAmount().longValue());
        exchange.setCurrencyId(Long.valueOf(orderCreated.getCurrencyId()));
        exchange.setOrderId(orderCreated.getId());

        // FOCUS: 랜덤하게 환률이 바뀌는 상황 이라면...
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

    // FOCUS: exchange 마이크로 서비스가 죽어있는 동안 발생한 취소 (deadline)에 대해서는 compensate 가 통과해 버릴 수 있음. 따라서 오류를 내어 재시도 시킴
    //////  혹은, 이를 통과 시키고, 위의 exchange 이벤트에서 deadline 을 초과한 거래에 대해서는 처리를 하지 않도록 2중 처리를 해줘야 함. 
    // this will be finally processed by Kafka event
    public static void compensate(OrderRejected orderRejected) {
        
        repository().findByOrderId(orderRejected.getId()).ifPresentOrElse(exchange->{
            
            repository().delete(exchange);

         }, ()->{
            throw new RuntimeException("No exchange transaction has been made for orderId" + orderRejected.getId());
         }
        );

    }
}
