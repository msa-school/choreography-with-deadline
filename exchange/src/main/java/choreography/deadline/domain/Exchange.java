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


    // @PrePersist
    // public void delay() {
    // }

    public static ExchangeRepository repository() {
        ExchangeRepository exchangeRepository = ExchangeApplication.applicationContext.getBean(
            ExchangeRepository.class
        );
        return exchangeRepository;
    }

    public static void exchange(OrderCreated orderCreated) {


        if("100".equals(orderCreated.getCurrencyId()))
        try{
            Thread.sleep(10000);
        }catch(Exception e){}


        ///if(orderCreated.getTimestamp() 가 deadline 을 넘으면 skip 혹은 order 상태를 읽어봐서 이미 rejected 면 취소.
        //FOCUS: time out 인것은 처리하지 않도록 하면 1차적으로 막을 수 있으나, 이미 이 라인을 넘어 처리 쓰레드에 들어간 상태에서 
        /// 그 짧은 순간에 하필 deadline 이 걸리면 어차피 안됨. 
        // 그건 deadline 을 살짝 넘은 처리건이므로 정상처리로 인정하는 방법 --(1)과
        // Order 쪽에서 Rejected 된게 approve 를 다시 타고 온 건에 대하여 다시 취소하게 하는 방법 --(2) 
        // 두가지가 있을거 같고 주로 (1)을 택할거 같다.

        Date now = new Date();
        if(orderCreated.getTimestamp() + 5000 < now.getTime()) return;  // FOCUS: skip the old OrderCreated events
        
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

    // FOCUS: exchange 마이크로 서비스가 죽어있는 동안 발생한 취소 (deadline)에 대해서는 compensate 가 통과해 버릴 수 있음.
    // 하지만, 앞서서 exchange 가 순서적으로 먼저 호출될 것이므로 (OrderCreated에 의해) 걱정할 것은 아님 . 
    // 혹은 아래 주석을 해제하여 오류를 내어 재시도 시킬 수 있음 (카프카는 기본 3번 정도 재시도하고 처리된걸로 해버리니 DLQ 처리는 따로 해줄것!)
    //////  혹은, 이를 통과 시키고, 위의 exchange 이벤트에서 deadline 을 초과한 거래에 대해서는 처리를 하지 않도록 2중 처리를 해줘야 함. 
    // this will be finally processed by Kafka event
    public static void compensate(OrderRejected orderRejected) {
        
        repository().findByOrderId(orderRejected.getId()).ifPresent/*OrElse*/(exchange->{
            
            repository().delete(exchange);

         }
          
        //, ()->{
        //     throw new RuntimeException("No exchange transaction is found for orderId" + orderRejected.getId());
        //  }
        );

    }
}
