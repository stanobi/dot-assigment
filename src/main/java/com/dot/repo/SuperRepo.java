package com.dot.repo;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class SuperRepo {

    EntityManagerFactory emf = null;
    EntityManager entityManager = null;
    EntityTransaction transaction = null;
    static final int MAX_BATCH_SIZE = 500;

    public EntityManager getEntityManager(){
        emf = Persistence.createEntityManagerFactory("req-limit-pu");
        return emf.createEntityManager();
    }

}
