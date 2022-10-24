package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class ExchangeSucceed extends AbstractEvent {

    private Long id;
    private Long productId;
    private Long stock;
    private Long orderId;
    private String userId;
    private Double pointUsed;
}
