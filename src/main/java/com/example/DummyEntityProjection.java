package com.example;

import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class DummyEntityProjection {

    private final EntityManager entityManager;

    @QueryHandler
    public DummyEntity handle(DummyEntityQuery query) {
        if(query.getId() == 999) throw new IllegalArgumentException("don't ask for 999");
        return entityManager.find(DummyEntity.class, query.getId());
    }

}
