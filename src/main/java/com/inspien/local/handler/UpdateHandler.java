package com.inspien.local.handler;

import com.inspien.local.domain.Directory;
import com.inspien.local.domain.FileInfo;
import com.inspien.local.domain.UpdateImfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;


@Component
public class UpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

    @Value("${url.server}")
    private String serverUrl;

    @Value("${url.directory}")
    private String LocalUrl;

    public void backUp(UpdateImfo updateImfo) {
        if (!updateImfo.getDeleteFileInfoList().isEmpty()) {
            for (FileInfo fileInfo : updateImfo.getDeleteFileInfoList()){
                deleteFile(fileInfo);
            }
        }

        // 최상위 폴더부터 삭제
        Collections.sort(updateImfo.getDeleteDirectoryList(), new Comparator<Directory>() {
            @Override
            public int compare(Directory o1, Directory o2) {
                int count1 = countSlashes(o1.getPath());
                int count2 = countSlashes(o2.getPath());
                // 역순으로 정렬하려면 count2 - count1로 변경
                return count2 - count1;
            }
            private int countSlashes(String path) {
                return path.length() - path.replace("/", "").length();
            }
        });


        if (!updateImfo.getDeleteDirectoryList().isEmpty()) {
            for (Directory directory : updateImfo.getDeleteDirectoryList()){
                deleteDirectory(directory);
            }
        }

        if (!updateImfo.getAddDirectoryList().isEmpty()){
            for (Directory directory : updateImfo.getAddDirectoryList()){
                uploadDirectory(directory);
            }
        }
        if (!updateImfo.getAddFileInfoList().isEmpty()){
            for (FileInfo fileInfo : updateImfo.getAddFileInfoList()) {
                uploadData(fileInfo);
            }
        }
    }

    //파일보내기
    public void uploadData(FileInfo fileInfo){
        File file = new File(LocalUrl + fileInfo.getPath());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("filepath", fileInfo.getPath());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(serverUrl+"fileUpload", requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("파일업로드 성공!");
        } else {
            logger.info("파일업로드 실패!" + fileInfo);
        }
    }
    //폴더생성
    public void uploadDirectory(Directory directory) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("mkdir", directory.getPath());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(serverUrl+"direcotryUpload", requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("폴더생성 성공!");
        } else {
            logger.info("폴더생성 실패!" + directory);
        }

    }

    //파일삭제
    public void deleteFile(FileInfo fileInfo) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileInfo.getPath());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(serverUrl+"deleteFile", requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("파일삭제 성공!" + fileInfo);
        } else {
            logger.info("파일삭제 실패!" + fileInfo);
        }
    }

    //폴더삭제
    public void deleteDirectory(Directory directory){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("mkdir", directory.getPath());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(serverUrl+"deleteDirectory", requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            logger.info("폴더삭제 성공!" + directory);
        } else {
            logger.info("폴더삭제 실패!" + directory);
        }
    }

}
