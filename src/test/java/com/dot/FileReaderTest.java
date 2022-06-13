package com.dot;

import com.dot.entity.UserAccessLog;
import com.dot.exceptions.AppException;
import com.dot.repo.SuperRepo;
import com.dot.repo.UserAccessLogRepo;
import com.dot.util.AppConstant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

class FileReaderTest {

    private final SuperRepo superRepo = new SuperRepo();
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private static final Logger log = LoggerFactory.getLogger(FileReaderTest.class);

    @BeforeEach
    void setUp() {
        clearTable();
    }

    @AfterEach
    void tearDown() {
        clearTable();
    }

    @Test
    void given_ValidInput_when_setFileReaderParamsFromInputParams_should_SetParamsSuccessfully() throws Exception {

        FileReader fileReader = new FileReader();
        Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", "--accessFile=/path/to/file",
                "--start=2022-01-01.13:00:00", "--duration=hourly", "—limit=100");
        Assertions.assertEquals("/path/to/file", fileReader.getAccessFile());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2022, Calendar.JANUARY, 1, 13, 0, 0);
        Assertions.assertEquals("/path/to/file", fileReader.getAccessFile());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(AppConstant.START_DATE_FORMAT);
        Assertions.assertEquals( simpleDateFormat.format(calendar.getTime()), simpleDateFormat.format(fileReader.getStart()));
        Assertions.assertEquals(AppConstant.HOURLY, fileReader.getDuration());
        Assertions.assertEquals(100, fileReader.getLimit());

    }

    @Test
    void given_InValidInput_when_setFileReaderParamsFromInputParams_should_throwExceptions() {

        FileReader fileReader = new FileReader();
        Assertions.assertThrows(AppException.class, () -> Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", ""));
        Assertions.assertThrows(AppException.class, () -> Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", "--accessFile=/path/to/file"));
        Assertions.assertThrows(AppException.class, () -> Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", "--accessFile=/path/to/file", "--start=2022-01-01.13:00:00"));
        Assertions.assertThrows(AppException.class, () -> Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", "--accessFile=/path/to/file", "--start=2022-01-01.13:00:00", "--duration=hourly"));
        Assertions.assertThrows(AppException.class, () -> Whitebox.invokeMethod(fileReader, "setFileReaderParamsFromInputParams", "--accessFile=/path/to/file", "--start=2022-01-01.13:00:00", "--duration=generally", "—limit=100"));

    }

    @Test
    void when_loadFileIntoDatabase_should_loadDataSuccessfully() throws Exception {

        FileReader fileReader = new FileReader();
        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        Whitebox.invokeMethod(fileReader, "loadFileIntoDatabase", userAccessLogRepo, "src/test/resources/user_access_log.txt");
        Assertions.assertEquals(5, userAccessLogRepo.getCount());

    }

    @Test
    void when_generateUserAccessLog_should_returnUserAccessLog() throws Exception {

        FileReader fileReader = new FileReader();
        String[] items = new String[5];
        items[0] = "2022-01-01 00:00:23.003";
        items[1] = "192.168.169.194";
        items[2] = "GET / HTTP/1.1";
        items[3] = "200";
        items[4] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393";
        Object object = Whitebox.invokeMethod(fileReader, "generateUserAccessLog", items);
        UserAccessLog userAccessLog = (UserAccessLog) object;
        Assertions.assertNotNull(userAccessLog);
        Assertions.assertNotNull(userAccessLog.getAccessDate());
        Assertions.assertEquals(items[4], userAccessLog.getUserAgent());
        Assertions.assertEquals(items[1], userAccessLog.getIp());
        Assertions.assertEquals(items[2], userAccessLog.getRequest());
        Assertions.assertEquals(items[3], userAccessLog.getStatus());

    }

    @Test
    void when_isUserAccessLogTableLoaded_should_returnTrue() throws Exception {

        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        UserAccessLog userAccessLog = getUserAccessLog();
        userAccessLogRepo.save(userAccessLog);

        FileReader fileReader = new FileReader();
        Object object = Whitebox.invokeMethod(fileReader, "isUserAccessLogTableLoaded", userAccessLogRepo);
        Assertions.assertTrue((Boolean) object);
    }

    @Test
    void given_startDateAndDuration_when_getEndDate_should_returnEndDateSuccessfully() throws Exception {
        FileReader fileReader = new FileReader();
        Date currentDate = new Date();

        Object objectHourly = Whitebox.invokeMethod(fileReader, "getEndDate", currentDate, AppConstant.HOURLY);
        Date date = (Date) objectHourly;
        Calendar instance = Calendar.getInstance();
        instance.setTime(currentDate);
        instance.add(Calendar.HOUR_OF_DAY, 1);
        Assertions.assertEquals(date, instance.getTime());

        Object objectDaily = Whitebox.invokeMethod(fileReader, "getEndDate", currentDate, AppConstant.DAILY);
        Date dateDaily = (Date) objectDaily;
        Calendar instanceDaily = Calendar.getInstance();
        instanceDaily.setTime(currentDate);
        instanceDaily.add(Calendar.DAY_OF_WEEK, 1);
        Assertions.assertEquals(dateDaily,instanceDaily.getTime());
    }

    @Test
    void given_searchParameters_when_fetchAndDisplayAllBlockedIpAlongsideDetails_should_displayAll() {

        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        UserAccessLog userAccessLog = getUserAccessLog();
        userAccessLogRepo.save(userAccessLog);

        FileReader fileReader = new FileReader();
        Assertions.assertDoesNotThrow(() -> Whitebox.invokeMethod(fileReader, "fetchAndDisplayAllBlockedIpAlongsideDetails", userAccessLogRepo, userAccessLog.getAccessDate(), new Date(), 0));

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
            entityManager.createQuery("delete from BlockedIPTable ").executeUpdate();
            entityManager.createQuery("delete from UserAccessLog ").executeUpdate();
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
}