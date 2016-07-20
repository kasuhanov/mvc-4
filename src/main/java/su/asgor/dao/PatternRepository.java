package su.asgor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import su.asgor.model.Pattern;

@Transactional
@Repository
public interface PatternRepository extends JpaRepository<Pattern,String> {
    Pattern findByPattern(String pattern);
}
