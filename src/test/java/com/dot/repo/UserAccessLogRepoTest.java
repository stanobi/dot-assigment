package com.dot.repo;

import com.dot.entity.BlockedIPTable;
import com.dot.entity.UserAccessLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

class UserAccessLogRepoTest {

    private final SuperRepo superRepo = new SuperRepo();
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private static final Logger log = LoggerFactory.getLogger(UserAccessLogRepoTest.class);

    @BeforeEach
    void setUp() {
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    @Test
    void given_UserAccessLog_when_save_should_insertSuccessfully() {

        Assertions.assertEquals(0, getCountOfItem());
        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        UserAccessLog userAccessLog = getUserAccessLog();
        userAccessLogRepo.save(userAccessLog);
        Assertions.assertEquals(1, getCountOfItem());

    }

    @Test
    void given_UserAccessLogs_when_save_should_insertAllSuccessfully() {

        Assertions.assertEquals(0, getCountOfItem());
        List<UserAccessLog> userAccessLogList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            UserAccessLog userAccessLog = getUserAccessLog();
            userAccessLogList.add(userAccessLog);
        }
        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        userAccessLogRepo.save(userAccessLogList);
        Assertions.assertEquals(1000, getCountOfItem());

    }

    @Test
    void given_searchValues_when_getAllIPByDurationAndLimit_should_fetchDataSuccessfully() {

        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        UserAccessLog userAccessLog = getUserAccessLog();
        userAccessLogRepo.save(userAccessLog);
        List<BlockedIPTable> blockedIPTables = userAccessLogRepo.getAllIPByDurationAndLimit(userAccessLog.getAccessDate(), new Date(), 0);
        Assertions.assertFalse(blockedIPTables.isEmpty());
        Assertions.assertEquals(1, blockedIPTables.size());

    }

    @Test
    void when_getCount_should_returnValueSuccessfully() {

        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        UserAccessLog userAccessLog = getUserAccessLog();
        userAccessLogRepo.save(userAccessLog);
        Assertions.assertEquals(1, userAccessLogRepo.getCount());

    }

    private UserAccessLog getUserAccessLog(){
        UserAccessLog userAccessLog = new UserAccessLog();
        userAccessLog.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
        userAccessLog.setIp("192.168.169.194");
        userAccessLog.setAccessDate(new Date());
        userAccessLog.setStatus("200");
        userAccessLog.setRequest("GET / HTTP/1.1");
        return userAccessLog;
    }

    private void clearTable() {
        try {
            entityManager = superRepo.getEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createQuery("delete from UserAccessLog ").executeUpdate();
            transaction.commit();
        } catch (Exception e){
            log.error("Unable to truncate table 'UserAccessLog' ", e);
            if (Objects.nonNull(transaction)) {
                transaction.rollback();
            }
        } finally {
            if (Objects.nonNull(entityManager)) {
                entityManager.close();
            }
        }
    }

    private long getCountOfItem() {
        long count = 0L;
        try {
            entityManager = superRepo.getEntityManager();
            count = entityManager.createQuery("select count(u.id) from UserAccessLog u", Long.class).getSingleResult();
        } catch (Exception e) {
            log.error("Unable to fetch count of rows in 'UserAccessLog'", e);
        } finally {
            entityManager.close();
        }
        return count;
    }
}