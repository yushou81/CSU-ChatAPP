package com.ys.controller;

import com.ys.model.FileEntity;
import com.ys.service.client.FileClient;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class FileManagementController {

    @FXML
    private ListView<FileEntity> filelistview;

    @FXML
    public void initialize() {
        // 假设userId为1，获取用户的文件列表
        FileClient.fetchFileList(1, filelistview);

        // 自定义ListView的显示方式
        filelistview.setCellFactory(new Callback<ListView<FileEntity>, ListCell<FileEntity>>() {
            @Override
            public ListCell<FileEntity> call(ListView<FileEntity> param) {
                return new ListCell<FileEntity>() {
                    @Override
                    protected void updateItem(FileEntity file, boolean empty) {
                        super.updateItem(file, empty);
                        if (file != null) {
                            setText("ID: " + file.getFileId() + " | Size: " + file.getFileSize() +
                                    " | Type: " + file.getFileType() + " | Path: " + file.getFilePath());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
    }
}
