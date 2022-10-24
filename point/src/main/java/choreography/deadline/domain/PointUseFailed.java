package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class PointUseFailed extends AbstractEvent {

    private Long id;
    private String reason;
    private Long orderId;

    public PointUseFailed(Point aggregate) {
        super(aggregate);
    }

    public PointUseFailed() {
        super();
    }
}
