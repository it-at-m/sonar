package de.muenchen.oss.sonar.backend.projekt;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjektRepository
        extends PagingAndSortingRepository<Projekt, UUID>, CrudRepository<Projekt, UUID>, JpaSpecificationExecutor<Projekt> {

}
