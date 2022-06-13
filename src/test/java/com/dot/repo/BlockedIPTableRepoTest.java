package com.dot.repo;

import com.dot.entity.BlockedIPTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class BlockedIPTableRepoTest {

    private final SuperRepo superRepo = new SuperRepo();
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private static final Logger log = LoggerFactory.getLogger(BlockedIPTableRepoTest.class);

    @BeforeEach
    void setUp() {
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    @Test
    void given_BlockedIPTable_when_save_should_insertSuccessfully() {

        Assertions.assertEquals(0, getCount());
        BlockedIPTableRepo blockedIPTableRepo = new BlockedIPTableRepo();
        BlockedIPTable blockedIPTable = new BlockedIPTable();
        blockedIPTable.setComment("TestComment");
        blockedIPTable.setIp("testIp");
        blockedIPTable.setRequestNumber(100L);
        blockedIPTableRepo.save(blockedIPTable);
        Assertions.assertEquals(1, getCount());

    }

    @Test
    void given_BlockedIPTables_when_save_should_insertAllSuccessfully() {

        Assertions.assertEquals(0, getCount());
        List<BlockedIPTable> blockedIPTableList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            BlockedIPTable blockedIPTable = new BlockedIPTable();
            blockedIPTable.setComment("TestComment");
            blockedIPTable.setIp("testIp");
            blockedIPTable.setRequestNumber(100L);
            blockedIPTableList.add(blockedIPTable);
        }

        BlockedIPTableRepo blockedIPTableRepo = new BlockedIPTableRepo();
        blockedIPTableRepo.save(blockedIPTableList);
        Assertions.assertEquals(1000, getCount());

    }

    private void clearTable() {
        try {
            entityManager = superRepo.getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createQuery("delete from BlockedIPTable ").executeUpdate();
            transaction.commit();
        } catch (Exception e){
            log.error("Unable to truncate table 'BlockedIPTable' ", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }
    }

    private long getCount() {
        long count = 0L;
        try {
            entityManager = superRepo.getEntityManager();
            count = entityManager.createQuery("select count(b.id) from BlockedIPTable b", Long.class).getSingleResult();
        } catch (Exception e) {
            log.error("Unable to fetch count of rows in 'BlockedIPTable'", e);
        } finally {
            entityManager.close();
        }
        return count;
    }
}