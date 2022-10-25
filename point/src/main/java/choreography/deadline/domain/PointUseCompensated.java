package choreography.deadline.domain;

import choreography.deadline.domain.*;
import choreography.deadline.infra.AbstractEvent;
import java.util.*;
import lombok.*;

@Data
@ToString
public class PointUseCompensated extends AbstractEvent {

    private Long id;
    private String reason;
    private Long orderId;

    public PointUseCompensated(Point aggregate) {
        super(aggregate);
    }

    public PointUseCompensated() {
        super();
    }
}
