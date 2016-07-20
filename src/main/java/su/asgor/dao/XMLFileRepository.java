package su.asgor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import su.asgor.model.XMLFile;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface XMLFileRepository extends JpaRepository<XMLFile,Long>{
    
}
