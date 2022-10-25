package choreography.deadline.domain;

import choreography.deadline.DeadlineApplication;
import java.util.Date;
import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "Deadline_table")
@Data
public class Deadline {
    static final int deadlineDurationInMS = 5 * 1000;  //FOCUS: 데드라인 5초

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date deadline;

    private Date startedTime;

    private Long orderId;

    public static DeadlineRepository repository() {
        DeadlineRepository deadlineRepository = DeadlineApplication.applicationContext.getBean(
            DeadlineRepository.class
        );
        return deadlineRepository;
    }

    public static void schedule(OrderCreated orderCreated) {
        Deadline deadline = new Deadline();
        deadline.setOrderId(orderCreated.getId());
        deadline.setStartedTime(new Date(orderCreated.getTimestamp()));


        //시행 당시의 deadline 이 기준이냐, 코드 적용 후 deadline 이 기준이냐 따라, 이때 deadline 을 저장할 수도 있고, 아닐 수도 있다.
        
        Date deadlineDate = new Date(deadline.getStartedTime().getTime() + deadlineDurationInMS);
        deadline.setDeadline(deadlineDate);

        
        repository().save(deadline);

    }

    public static void delete(OrderPlaced orderPlaced) {
        repository().findByOrderId(orderPlaced.getId()).ifPresentOrElse(deadline ->{
            repository().delete(deadline);
        }, ()->{throw new RuntimeException("No such order id" + orderPlaced.getId());});

    }

    public static void delete(OrderRejected orderRejected) {
        repository().findByOrderId(orderRejected.getId()).ifPresentOrElse(deadline ->{
            repository().delete(deadline);
        }, ()->{throw new RuntimeException("No such order id" + orderRejected.getId());});

    }

    public static void sendDeadlineEvents(){

        repository().findAll().forEach(deadline ->{
            Date now = new Date();
            
            if(now.after(deadline.getDeadline())){
                repository().delete(deadline); //FOCUS: 한번 보내면 deadline 없애야 
                new DeadlineReached(deadline).publishAfterCommit();

            }
        });

    }
}
