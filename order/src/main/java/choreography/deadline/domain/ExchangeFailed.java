package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class ExchangeFailed extends AbstractEvent {

    private Long id;
    private Long orderId;
}
