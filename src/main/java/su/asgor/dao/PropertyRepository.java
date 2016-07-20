package su.asgor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.asgor.model.Property;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Property findByName(String name);
}