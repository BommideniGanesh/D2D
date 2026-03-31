package com.example.demo.billing.batch;

import com.example.demo.billing.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Configuration
public class InvoiceBatchConfig {

    @Bean
    public Job monthlyInvoicingJob(JobRepository jobRepository, Step generateInvoicesStep) {
        return new JobBuilder("monthlyInvoicingJob", jobRepository)
                .start(generateInvoicesStep)
                .build();
    }

    @Bean
    public Step generateInvoicesStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     ItemReader<B2BClient> clientReader,
                                     ItemProcessor<B2BClient, Invoice> invoiceProcessor,
                                     ItemWriter<Invoice> invoiceWriter) {
        return new StepBuilder("generateInvoicesStep", jobRepository)
                .<B2BClient, Invoice>chunk(10, transactionManager)
                .reader(clientReader)
                .processor(invoiceProcessor)
                .writer(invoiceWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<B2BClient> clientReader(B2BClientRepository repository) {
        return new RepositoryItemReaderBuilder<B2BClient>()
                .name("clientReader")
                .repository(repository)
                .methodName("findAll")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<B2BClient, Invoice> invoiceProcessor(ClientWalletRepository walletRepo,
                                                              WalletTransactionRepository transRepo) {
        return client -> {
            ClientWallet wallet = walletRepo.findByB2bClient(client).orElse(null);
            if (wallet == null) return null;

            var transactions = transRepo.findByWalletIdAndIsInvoicedFalse(wallet.getId());
            if (transactions.isEmpty()) return null;

            BigDecimal totalOwed = transactions.stream()
                    .filter(t -> t.getTransactionType() == WalletTransaction.TransactionType.DEBIT)
                    .map(WalletTransaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalOwed.compareTo(BigDecimal.ZERO) <= 0) return null;

            // Mark as invoiced
            for (WalletTransaction t : transactions) {
                t.setInvoiced(true);
                transRepo.save(t);
            }

            return Invoice.builder()
                    .client(client)
                    .totalAmount(totalOwed)
                    .billingPeriod(YearMonth.now().toString())
                    .build();
        };
    }

    @Bean
    public ItemWriter<Invoice> invoiceWriter(InvoiceRepository invoiceRepository) {
        return invoices -> invoiceRepository.saveAll(invoices);
    }
}
