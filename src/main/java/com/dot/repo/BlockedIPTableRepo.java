package com.dot.repo;

import com.dot.entity.BlockedIPTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class BlockedIPTableRepo extends SuperRepo {

    private static final Logger log = LoggerFactory.getLogger(BlockedIPTableRepo.class);

    public void save(BlockedIPTable blockedIPTable) {
        try {
            entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(blockedIPTable);
            transaction.commit();
        } catch (Exception e) {
            log.error("Unable to save blocked_ip_table item", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }
    }

    public void save(List<BlockedIPTable> blockedIPTables) {
        try {
            entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            int counter = 0;
            for (BlockedIPTable blockedIPTable : blockedIPTables) {
                entityManager.persist(blockedIPTable);
                counter+=1;
                if (counter%MAX_BATCH_SIZE == 0) {
                    transaction.commit();
                    entityManager.clear();
                    transaction.begin();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("Unable to save blocked_ip_table bulk items", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }
    }

}
