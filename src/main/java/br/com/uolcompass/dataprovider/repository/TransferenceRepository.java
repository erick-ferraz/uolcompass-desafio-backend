package br.com.uolcompass.dataprovider.repository;

import br.com.uolcompass.dataprovider.database.entity.TransferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferenceRepository extends JpaRepository<TransferenceEntity, Long> {

    @Query("SELECT t FROM TransferenceEntity t " +
           "WHERE t.payer.id = :walletId OR t.payee.id = :walletId " +
           "ORDER BY t.createdAt DESC")
    List<TransferenceEntity> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") Long walletId);
}
