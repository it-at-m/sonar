package de.muenchen.oss.sonar.backend.projekt;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjektRepository extends CrudRepository<Projekt, UUID> {

}
