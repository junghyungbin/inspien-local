package com.inspien.local.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImfo {

    private List<FileInfo> addFileInfoList;

    private List<Directory> addDirectoryList;

    private List<FileInfo> deleteFileInfoList;

    private List<Directory> deleteDirectoryList;

    public boolean isAllListsEmpty(){
        return addFileInfoList.isEmpty() &&
                addDirectoryList.isEmpty() &&
                deleteFileInfoList.isEmpty() &&
                deleteDirectoryList.isEmpty();
    }
}
