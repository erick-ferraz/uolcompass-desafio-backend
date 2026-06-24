package br.com.uolcompass.dataprovider.repository;

import br.com.uolcompass.dataprovider.database.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByPublishedFalseOrderByCreatedAtAsc();
}
