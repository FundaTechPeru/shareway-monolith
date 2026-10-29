package com.fundatechperu.shareway.payments.bdd;

import io.cucumber.java.Before;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Vacía las tablas de Payments &amp; Settlement antes de cada escenario para que sean
 * independientes. Solo toca las entidades de este contexto.
 */
public class DatabaseHooks {

  private static final String PAYMENTS_PACKAGE = "com.fundatechperu.shareway.payments";

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;

  public DatabaseHooks(EntityManager entityManager, TransactionTemplate transactionTemplate) {
    this.entityManager = entityManager;
    this.transactionTemplate = transactionTemplate;
  }

  @Before
  public void cleanDatabase() {
    transactionTemplate.executeWithoutResult(
        status -> {
          for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
            if (entity.getJavaType().getPackageName().startsWith(PAYMENTS_PACKAGE)) {
              entityManager.createQuery("delete from " + entity.getName()).executeUpdate();
            }
          }
        });
  }
}
