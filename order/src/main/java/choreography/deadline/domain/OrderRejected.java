package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class OrderRejected extends AbstractEvent {

    private Long id;
    private String currencyId;
    private Double amount;
    private String holderId;


    public OrderRejected(Order aggregate) {
        super(aggregate);
    }

    public OrderRejected() {
        super();
    }
}
