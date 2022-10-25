package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class ExchangeCompensated extends AbstractEvent {

    private Long id;
    private Long productId;
    private Long stock;
    private Long orderId;
    private Double point;

    public ExchangeCompensated(Exchange aggregate) {
        super(aggregate);
    }

    public ExchangeCompensated() {
        super();
    }
}
