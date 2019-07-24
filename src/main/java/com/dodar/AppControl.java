package com.dodar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AppControl implements Initializable {

    /**
     * 选择数据文件路径
     */
    @FXML
    private TextField filepath;
    /**
     * 选择SQLite文件路径
     */
    @FXML
    private TextField sqlitepath;
    /**
     * 数据库登录名
     */
    @FXML
    private TextField sqlitename;
    /**
     * 数据库密码
     */
    @FXML
    private TextField sqlitepass;
    /**
     * 行-数据分隔符
     */
    @FXML
    private TextField rowflag;
    /**
     * 列-数据分隔符
     */
    @FXML
    private TextField colflag;
    /**
     * 列分隔数据库字段英文名称
     */
    @FXML
    private TextField fieldnames;
    /**
     * 读取Text数据文件后展示的内容列表
     */
    @FXML
    private TableView datatable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * 数据文件目录选择
     * @param e
     */
    @FXML
    protected void directorySelect(ActionEvent e) {
        System.out.println("you click me" + e);
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(new Stage());
        String path = file.getPath();
        filepath.setText(path);
    }

    /**
     * Sqlite 数据库文件选择
     * @param e
     */
    @FXML
    public void sqliteSelect(ActionEvent e) {

    }

    /**
     * 默认参数配置更新
     * @param e
     */
    @FXML
    public void updateConfig(ActionEvent e) {

    }

    /**
     * 读取TEXT数据文件并转换成数据库对象
     * @param e
     */
    @FXML
    public void readFilesToData(ActionEvent e) {

    }

    /**
     * 保存数据到Sqlite数据库
     * @param e
     */
    @FXML
    public void saveDataSqlite(ActionEvent e) {

    }

}
