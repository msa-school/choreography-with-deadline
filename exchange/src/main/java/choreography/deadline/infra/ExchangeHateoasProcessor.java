package choreography.deadline.infra;

import choreography.deadline.domain.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
public class ExchangeHateoasProcessor
    implements RepresentationModelProcessor<EntityModel<Exchange>> {

    @Override
    public EntityModel<Exchange> process(EntityModel<Exchange> model) {
        return model;
    }
}
