package de.muenchen.oss.sonar.backend.widerspruch;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WiderspruchRepository extends CrudRepository<WiderspruchEntity, UUID> {

}
