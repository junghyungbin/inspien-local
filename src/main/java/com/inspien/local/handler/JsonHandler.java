package com.inspien.local.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspien.local.domain.*;
import com.inspien.local.service.BackUpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


@Component
public class JsonHandler {

    private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    @Value("${url.json}")
    private String jsonfile;
    public UpdateImfo createJson(JsonFile recentData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File toJsonFile = new File(jsonfile);
        if (toJsonFile.exists()){
            UpdateImfo updateImfo = compareJson(recentData);
            if(!updateImfo.isAllListsEmpty()){
                logger.info("변화있음");
                String stringJson = new String(Files.readAllBytes(Paths.get(toJsonFile.getAbsolutePath())));
                JsonList jsonMap = objectMapper.readValue(stringJson, JsonList.class);
                jsonMap.getJsonFileList().add(recentData);
                objectMapper.writeValue(toJsonFile, jsonMap);
                return updateImfo;
            } else{
                logger.info("변화없음");
                return null;
            }
        }else {
            ArrayList<JsonFile> firstJsone = new ArrayList<>();
            firstJsone.add(recentData);
            JsonList jsonList = new JsonList(firstJsone);
            objectMapper.writeValue(toJsonFile, jsonList);
            UpdateImfo updateImfo = new UpdateImfo(recentData.getFileInfoList(), recentData.getDirectoryList(), new ArrayList<>(), new ArrayList<>());
            return updateImfo;
        }
    }

    public UpdateImfo compareJson(JsonFile recentData) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonData = new File(jsonfile);
        String stringJson = new String(Files.readAllBytes(Paths.get(jsonData.getAbsolutePath())));
        JsonList jsonMap = objectMapper.readValue(stringJson, JsonList.class);
        JsonFile pastData = jsonMap.getJsonFileList().get(jsonMap.getJsonFileList().size()-1);

        ArrayList<FileInfo> addFileList = new ArrayList<>();
        ArrayList<FileInfo> deleteFileList = new ArrayList<>();

        ArrayList<Directory> addDirectoryList = new ArrayList<>();
        ArrayList<Directory> deleteDirectoryList = new ArrayList<>();


        for (FileInfo recent : recentData.getFileInfoList()){
            if (!pastData.getFileInfoList().contains(recent)) {
                addFileList.add(recent);
            }
        }
        for (FileInfo past : pastData.getFileInfoList()){
            if (!recentData.getFileInfoList().contains(past)) {
                deleteFileList.add(past);
            }
        }
        for (Directory recent : recentData.getDirectoryList()){
            if (!pastData.getDirectoryList().contains(recent)) {
                addDirectoryList.add(recent);
            }
        }
        for (Directory past : pastData.getDirectoryList()){
            if (!recentData.getDirectoryList().contains(past)) {
                deleteDirectoryList.add(past);
            }
        }

        UpdateImfo updateImfo = new UpdateImfo(addFileList, addDirectoryList, deleteFileList, deleteDirectoryList);

        return updateImfo;
    }
}
