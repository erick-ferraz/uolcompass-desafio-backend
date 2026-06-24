package br.com.uolcompass.entrypoints.config;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.WalletGateway;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = false)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final WalletGateway walletGateway;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!walletGateway.findAll().isEmpty()) {
            log.info("Database already contains wallets — skipping seeder");
            return;
        }

        log.info("Seeding database with initial wallets...");

        var wallets = List.of(
                new WalletDomain(null, "Diógenes", "11111111111", "diogenes@email.com",
                        passwordEncoder.encode("123456"), BigDecimal.valueOf(5000),
                        WalletType.INDIVIDUAL, null, null),
                new WalletDomain(null, "Babidi", "22222222222", "babidi@email.com",
                        passwordEncoder.encode("123456"), BigDecimal.valueOf(3000),
                        WalletType.INDIVIDUAL, null, null),
                new WalletDomain(null, "Bilbow", "33333333333", "bilbow@email.com",
                        passwordEncoder.encode("123456"), BigDecimal.valueOf(2000),
                        WalletType.INDIVIDUAL, null, null),
                new WalletDomain(null, "Dell Corp", "11111111111111", "dell@email.com",
                        passwordEncoder.encode("123456"), BigDecimal.valueOf(50000),
                        WalletType.BUSINESS, null, null),
                new WalletDomain(null, "Echo Ltda", "22222222222222", "echo@email.com",
                        passwordEncoder.encode("123456"), BigDecimal.valueOf(30000),
                        WalletType.BUSINESS, null, null)
        );

        for (var wallet : wallets) {
            var created = walletGateway.create(wallet);
            log.info("  Created wallet ID={} name={} type={} balance={}",
                    created.getId(), created.getName(), created.getType(), created.getBalance());
        }

        log.info("Seeder finished — {} wallets created", wallets.size());
    }
}
