package choreography.deadline.domain;

import choreography.deadline.domain.*;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "deadlines", path = "deadlines")
public interface DeadlineRepository
    extends PagingAndSortingRepository<Deadline, Long> {

    Optional<Deadline> findByOrderId(Long id);
}
