package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class DeadlineReached extends AbstractEvent {

    private Long id;
    private Date deadline;
    private Long orderId;
}
