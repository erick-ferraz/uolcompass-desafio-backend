package br.com.uolcompass.dataprovider.repository;

import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    boolean existsByCpfCnpj(String cpfCnpj);
}
