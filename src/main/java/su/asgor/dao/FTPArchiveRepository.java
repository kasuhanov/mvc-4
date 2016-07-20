package su.asgor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import su.asgor.model.FTPArchive;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface FTPArchiveRepository extends JpaRepository<FTPArchive,Long>, QueryDslPredicateExecutor<FTPArchive> {

}
