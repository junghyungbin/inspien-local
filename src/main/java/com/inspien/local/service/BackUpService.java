package com.inspien.local.service;

import com.inspien.local.domain.Directory;
import com.inspien.local.domain.FileInfo;
import com.inspien.local.domain.JsonFile;
import com.inspien.local.domain.UpdateImfo;
import com.inspien.local.handler.FileHashHandler;
import com.inspien.local.handler.JsonHandler;
import com.inspien.local.handler.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


@Service
public class BackUpService {

    private static final Logger logger = LoggerFactory.getLogger(BackUpService.class);

    @Value("${url.directory}")
    private String directoryUrl;

    @Autowired
    private JsonHandler jsonHandler;

    @Autowired
    private UpdateHandler updateHandler;


    //디렉토리 탐색
    private UpdateImfo watchDirectory(File baseDirectory) throws IOException, NoSuchAlgorithmException {
        File[] files = baseDirectory.listFiles();
        JsonFile jsonFile = new JsonFile(new ArrayList<>(),new ArrayList<>());
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals("test.json")|| file.isHidden() || file.getName().startsWith(".")){
                    continue;
                }
                if (file.isDirectory()){
                    Directory directory = new Directory(file.getName(), file.getAbsolutePath().replace(directoryUrl, ""));
                    jsonFile.getDirectoryList().add(directory);
                    toListFile(jsonFile, file);
                } else{
                    FileInfo fileInfo = new FileInfo(file.getName(), file.getAbsolutePath().replace(directoryUrl, ""), FileHashHandler.createHash(file.getAbsolutePath()));
                    jsonFile.getFileInfoList().add(fileInfo);
                }
            }
        }
        return jsonHandler.createJson(jsonFile);
    }

    //디렉토리시 재귀를 통한 탐색
    private void toListFile(JsonFile jsonFile, File file) throws IOException, NoSuchAlgorithmException {
        File[] files = file.listFiles();
        if(files != null) {
            for (File f : files) {
                if (f.isDirectory()){
                    Directory directory = new Directory(f.getName(), f.getAbsolutePath().replace(directoryUrl, ""));
                    jsonFile.getDirectoryList().add(directory);
                    toListFile(jsonFile, f);
                } else {
                    if (f.isHidden() || f.getName().startsWith(".")){
                        continue;
                    }
                    FileInfo fileInfo = new FileInfo(f.getName(), f.getAbsolutePath().replace(directoryUrl, ""), FileHashHandler.createHash(f.getAbsolutePath()));
                    jsonFile.getFileInfoList().add(fileInfo);
                }
            }
        }
    }


    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void runDbSaveService() throws IOException, NoSuchAlgorithmException {
        UpdateImfo updateImfo = watchDirectory(new File(directoryUrl));
        if (updateImfo != null){
            updateHandler.backUp(updateImfo);
        }

    }

}
