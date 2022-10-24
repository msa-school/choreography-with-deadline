package choreography.deadline.domain;

import choreography.deadline.domain.*;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "exchanges", path = "exchanges")
public interface ExchangeRepository
    extends PagingAndSortingRepository<Exchange, Long> {

    Optional<Exchange> findByOrderId(Long id);

}
