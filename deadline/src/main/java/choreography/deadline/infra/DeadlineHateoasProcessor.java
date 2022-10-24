package choreography.deadline.infra;

import choreography.deadline.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class DeadlineHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Deadline>> {

    @Override
    public EntityModel<Deadline> process(EntityModel<Deadline> model) {
        return model;
    }
}
