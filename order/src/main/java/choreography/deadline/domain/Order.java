package choreography.deadline.domain;

import choreography.deadline.OrderApplication;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Order_table")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String currencyId;

    private Double amount;

    private String status;

    private String holderId;

    
    @PostPersist
    public void onPostPersist() {
        setStatus("PENDING"); //FOCUS

        OrderCreated orderCreated = new OrderCreated(this);
        orderCreated.publishAfterCommit();
    }

    public static OrderRepository repository() {
        OrderRepository orderRepository = OrderApplication.applicationContext.getBean(
            OrderRepository.class
        );
        return orderRepository;
    }

    public static void reject(ExchangeFailed exchangeFailed) {
        
        repository().findById(exchangeFailed.getOrderId()).ifPresent(order->{
            
            order.setStatus("REJECTED DUE TO EXCHANGE ERROR");
            repository().save(order);

            OrderRejected orderRejected = new OrderRejected(order);
            orderRejected.publishAfterCommit();

         });

    }

    public static void reject(DeadlineReached deadlineReached) {
        
        repository().findById(deadlineReached.getOrderId()).ifPresent(order->{
            
            order.setStatus("REJECTED DUE TO DEADLINE");
            repository().save(order);

            OrderRejected orderRejected = new OrderRejected(order);
            orderRejected.publishAfterCommit();

         });

    }

    public static void reject(PointUseFailed pointUseFailed) {
        repository().findById(pointUseFailed.getOrderId()).ifPresent(order->{
            
            order.setStatus("REJECTED DUE TO POINT LIMIT");
            repository().save(order);

            OrderRejected orderRejected = new OrderRejected(order);
            orderRejected.publishAfterCommit();

         });

    }

    public static void approve(PointUsed pointUsed) {
        repository().findById(pointUsed.getId()).ifPresent(order->{
            
            order.setStatus("APPROVED");
            repository().save(order);

            OrderPlaced orderPlaced = new OrderPlaced(order);
            orderPlaced.publishAfterCommit();

         });
    }
}
