package br.com.uolcompass.dataprovider.repository;

import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("WalletRepositoryIT — integração com MySQL")
class WalletRepositoryIT {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and find wallet by ID")
    void shouldSaveAndFindWallet() {
        var wallet = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getBalance), BigDecimal.valueOf(1000))
                .set(field(WalletEntity::getVersion), 0L)
                .create();

        entityManager.persistAndFlush(wallet);

        var found = walletRepository.findById(wallet.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(wallet.getName());
        assertThat(found.get().getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should check existence by CPF/CNPJ")
    void shouldCheckExistenceByCpfCnpj() {
        var wallet = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getCpfCnpj), "99988877766")
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getVersion), 0L)
                .create();

        entityManager.persistAndFlush(wallet);

        assertThat(walletRepository.existsByCpfCnpj("99988877766")).isTrue();
        assertThat(walletRepository.existsByCpfCnpj("00000000000")).isFalse();
    }

    @Test
    @DisplayName("Should increment version on update (optimistic lock)")
    void shouldIncrementVersionOnUpdate() {
        var wallet = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getBalance), BigDecimal.valueOf(500))
                .set(field(WalletEntity::getVersion), 0L)
                .create();

        entityManager.persistAndFlush(wallet);
        assertThat(wallet.getVersion()).isZero();

        wallet.setBalance(BigDecimal.valueOf(700));
        entityManager.persistAndFlush(wallet);

        assertThat(wallet.getVersion()).isOne();
    }
}
