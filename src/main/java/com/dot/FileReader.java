package com.dot;

import com.dot.entity.BlockedIPTable;
import com.dot.entity.UserAccessLog;
import com.dot.exceptions.AppException;
import com.dot.repo.BlockedIPTableRepo;
import com.dot.repo.UserAccessLogRepo;
import com.dot.util.AppConstant;
import com.dot.util.InputConstant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class FileReader {

    private String accessFile;
    private Date start;
    private String duration;
    private Integer limit;
    String[] split = new String[5];

    private static final Logger log = LoggerFactory.getLogger(FileReader.class);

    public static void main(String[] args) throws AppException, IOException {

        FileReader fileReader = new FileReader();
        UserAccessLogRepo userAccessLogRepo = new UserAccessLogRepo();
        fileReader.setFileReaderParamsFromInputParams(args);
        fileReader.loadFileIntoDatabase(userAccessLogRepo, fileReader.getAccessFile());
        fileReader.fetchAndDisplayAllBlockedIpAlongsideDetails(userAccessLogRepo, fileReader.getStart(),
                fileReader.getEndDate(fileReader.getStart(), fileReader.getDuration()), fileReader.getLimit());

    }

    private void setFileReaderParamsFromInputParams(String... args) throws AppException {
        //set the params (AccessFile, start, duration, limit)
        for (String content : args) {
            setInput(content);
        }
        validateInput();
    }

    private void setInput(String content) throws AppException {
        if (content.contains(InputConstant.ACCESS_FILE)) {
            split = content.split(InputConstant.ACCESS_FILE);
            setAccessFile(split[split.length-1]);
        }

        setStartDate(content);

        if (content.contains(InputConstant.DURATION)) {
            split = content.split(InputConstant.DURATION);
            setDuration(split[split.length-1]);
        }

        if (content.contains(InputConstant.LIMIT)) {
            split = content.split(InputConstant.LIMIT);
            setLimit(Integer.valueOf(split[split.length-1]));
        }
    }

    private void setStartDate(String content) throws AppException {
        if (content.contains(InputConstant.START)) {
            split = content.split(InputConstant.START);
            String startInputData = split[split.length-1];
            try {
                setStart(new SimpleDateFormat(AppConstant.START_DATE_FORMAT).parse(startInputData));
            } catch (ParseException e) {
                log.error("Unable to parse start input : {}", startInputData, e);
                throw new AppException("Invalid Date provided, Expected format is '"+AppConstant.START_DATE_FORMAT+"'");
            }
        }
    }

    private void validateInput() throws AppException {
        //if any param is not provided then throw exception
        if (Objects.isNull(getStart())) {
            throw new AppException("start is required");
        }

        if (Objects.isNull(getDuration())) {
            throw new AppException("duration is required");
        }

        if (!getDuration().equals(AppConstant.HOURLY) && !getDuration().equals(AppConstant.DAILY)) {
            throw new AppException("Invalid duration is provided. duration is either '"+AppConstant.HOURLY+"' or '"+AppConstant.DAILY+"' ");
        }

        if (Objects.isNull(getLimit())) {
            throw new AppException("limit is required");
        }
    }

    private void loadFileIntoDatabase(UserAccessLogRepo userAccessLogRepo, String accessFile) throws IOException {

        //confirm if table in the database is empty
        if (isUserAccessLogTableLoaded(userAccessLogRepo)) {
            log.info("data already inserted to the database");
           return;
        }

        //insert all records from file into database
        List<UserAccessLog> userAccessLogList = new ArrayList<>();
        try (LineIterator it = FileUtils.lineIterator(new File(accessFile), "UTF-8")) {
            while (it.hasNext()) {
                String line = it.nextLine();
                //convert into Entity
                UserAccessLog userAccessLog = generateUserAccessLog(line.split("\\|"));
                userAccessLogList.add(userAccessLog);
            }
        }

        //save all item into the database
        userAccessLogRepo.save(userAccessLogList);
    }

    private UserAccessLog generateUserAccessLog(String... split){
        UserAccessLog userAccessLog = new UserAccessLog();
        if (split.length >= 1) {
            try {
                userAccessLog.setAccessDate(new SimpleDateFormat(AppConstant.ASSESS_DATE_FORMAT).parse(split[0]));
            } catch (ParseException e) {
                log.error("Unable to parse string to date : {} ", split[0], e);
            }
        }

        if (split.length >= 2) { userAccessLog.setIp(split[1]); }
        if (split.length >= 3) { userAccessLog.setRequest(split[2]); }
        if (split.length >= 4) { userAccessLog.setStatus(split[3]); }
        if (split.length >= 5) { userAccessLog.setUserAgent(split[4]); }

        return userAccessLog;
    }

    private boolean isUserAccessLogTableLoaded(UserAccessLogRepo userAccessLogRepo) {
        return userAccessLogRepo.getCount() > 0;
    }

    private Date getEndDate(Date startDate, String duration) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(startDate);
        //if duration is hourly
        if (AppConstant.HOURLY.equals(duration)) {
            //then endDate = startDate + 1 hour
            instance.add(Calendar.HOUR_OF_DAY, 1);
        }

        if (AppConstant.DAILY.equals(duration)) {
            //then endDate = startDate + 1 day
            instance.add(Calendar.DAY_OF_WEEK, 1);
        }

        return instance.getTime();
    }

    private void fetchAndDisplayAllBlockedIpAlongsideDetails(UserAccessLogRepo userAccessLogRepo,
                                                     Date startDate, Date endDate, int limit) {
        //query the database for all ip and count then add a comment
        List<BlockedIPTable> blockedIPTables = userAccessLogRepo.getAllIPByDurationAndLimit(startDate, endDate, limit);

        //save into the blockerIPTable
        BlockedIPTableRepo blockedIPTableRepo = new BlockedIPTableRepo();
        blockedIPTableRepo.save(blockedIPTables);

        //display all
        blockedIPTables.forEach(blockedIPTable -> log.info(blockedIPTable.toString()));
    }

    public String getAccessFile() {
        return accessFile;
    }

    public void setAccessFile(String accessFile) {
        this.accessFile = accessFile;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
