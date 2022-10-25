package choreography.deadline.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import choreography.deadline.PointApplication;
import lombok.Data;

@Entity
@Data
public class Transaction {
    @Id
    Long orderId;
    String userId;
    Double pointUsed;

    public static TransactionRepository repository(){
        return PointApplication.applicationContext.getBean(TransactionRepository.class);
    }
}
