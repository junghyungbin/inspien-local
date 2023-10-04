package com.inspien.local.domain;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonFile {

    private List<FileInfo> fileInfoList;

    private List<Directory> directoryList;

}
