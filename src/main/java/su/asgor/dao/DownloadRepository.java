package su.asgor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import su.asgor.model.Download;

import javax.transaction.Transactional;

@Transactional
@Repository
public interface DownloadRepository extends JpaRepository<Download,Long>, QueryDslPredicateExecutor<Download> {
    Download findFirst1ByOrderByDateDesc();
}
