package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Application implements CommandLineRunner {

    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;
    private final QueryGateway queryGateway;
    private final ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        loadTestFixture();
        queryForExistingEntity();
        queryForNonExistingEntity();
        queryForErrorTriggeringEntity();
        applicationContext.close();
    }

    private void loadTestFixture() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                val dummyEntity = new DummyEntity(1, "Hello, World!");
                log.info("Persisting {} as test background.", dummyEntity);
                entityManager.persist(dummyEntity);
            }
        });
    }

    private void queryForExistingEntity() {
        log.info("Querying for existing entity.");
        SubscriptionQueryResult<DummyEntity, DummyEntity> result =
                queryGateway.subscriptionQuery(new DummyEntityQuery(1), DummyEntity.class, DummyEntity.class);
        DummyEntity initialResult = result.initialResult().block();
        log.info("Got result: {}", initialResult); // will be the entity
    }

    private void queryForNonExistingEntity() {
        log.info("Querying for non-existing entity.");
        SubscriptionQueryResult<DummyEntity, DummyEntity> result =
                queryGateway.subscriptionQuery(new DummyEntityQuery(2), DummyEntity.class, DummyEntity.class);
        DummyEntity initialResult = result.initialResult().block();
        log.info("Got result: {}", initialResult); // will be null
    }

    private void queryForErrorTriggeringEntity() {
        log.info("Querying for entity that will trigger exception in query handler.");
        SubscriptionQueryResult<DummyEntity, DummyEntity> result =
                queryGateway.subscriptionQuery(new DummyEntityQuery(999), DummyEntity.class, DummyEntity.class);
        result.initialResult().doOnError(throwable -> {
            log.error("Got error: {}", throwable);
        });
        DummyEntity initialResult = result.initialResult().block();
        log.info("Got result: {}", initialResult); // will be null
    }
}

