package choreography.deadline.domain;

import choreography.deadline.DeadlineApplication;
import choreography.deadline.domain.DeadlineReached;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.Data;

@Entity
@Table(name = "Deadline_table")
@Data
public class Deadline {

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
        
        repository().save(deadline);

    }

    public static void delete(OrderPlaced orderPlaced) {
        repository().findByOrderId(orderPlaced.getId()).ifPresent(deadline ->{
            repository().delete(deadline);
        });

    }

    static final int deadlineDurationInMS = 60 * 1000;

    public static void sendDeadlineEvents(){

        repository().findAll().forEach(deadline ->{
            Date now = new Date();
            Date deadlineDate = new Date(deadline.getStartedTime().getTime() + deadlineDurationInMS);

            if(now.after(deadlineDate)){
                new DeadlineReached(deadline).publishAfterCommit();
            }
        });

    }
}
