package com.dot.repo;

import com.dot.entity.BlockedIPTable;
import com.dot.entity.UserAccessLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.TemporalType;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserAccessLogRepo extends SuperRepo {

    private static final Logger log = LoggerFactory.getLogger(UserAccessLogRepo.class);

    //save one
    public void save(UserAccessLog userAccessLog) {
        try {
            entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(userAccessLog);
            transaction.commit();
        } catch (Exception e) {
            log.error("Unable to save userAccessLog ", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)){
                entityManager.close();
            }
        }
    }

    //save all
    public void save(List<UserAccessLog> userAccessLogs) {

        try {
            entityManager = getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            int counter = 0;
            for (UserAccessLog userAccessLog : userAccessLogs) {
                entityManager.persist(userAccessLog);
                counter+=1;
                if (counter%MAX_BATCH_SIZE == 0) {
                    transaction.commit();
                    entityManager.clear();
                    transaction.begin();
                }
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("Unable to save all UserAccessLog ", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)){
                entityManager.close();
            }
        }

    }

    //find by startDate, endDate, limit
    public List<BlockedIPTable> getAllIPByDurationAndLimit(Date startDate, Date endDate, int limit){

        try {
            entityManager = getEntityManager();
            return entityManager.createQuery("select new com.dot.entity.BlockedIPTable(u.ip, count(u.id)) from UserAccessLog u where u.accessDate >= ?1 and u.accessDate <= ?2 group by u.ip having count(u.id) > ?3 ", BlockedIPTable.class)
                    .setParameter(1, startDate, TemporalType.TIMESTAMP)
                    .setParameter(2, endDate, TemporalType.TIMESTAMP)
                    .setParameter(3, (long) limit)
                    .getResultList();
        } catch (Exception e) {
            log.error("Unable to fetch ips and details ", e);
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }

        return Collections.emptyList();
    }

    public long getCount(){
        try{
            entityManager = getEntityManager();
            return entityManager.createQuery("select count(u.id) from UserAccessLog u", Long.class).getSingleResult();
        } catch (Exception e){
            log.error("Unable to fetch count of userAccessLog ", e);
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }

        return 0L;
    }

}
