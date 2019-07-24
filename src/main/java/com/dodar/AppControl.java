package com.dodar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class AppControl implements Initializable {

    static Logger logger = LogManager.getLogger("Console");

    /**
     * 属性配置对象
     */
    private Properties properties;
    private String propertyUrl;

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
        File file = new File("./convertor.properties");
        properties = new Properties();
        propertyUrl = file.getAbsolutePath();
        readConfig();  // 初始化读取配置
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
     * 读取配置文件
     */
    public void readConfig() {
        try {
            logger.info("准备读取配置");
            File file = new File(propertyUrl);
            FileInputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
            filepath.setText(properties.getProperty("filepath"));
            sqlitepath.setText(properties.getProperty("sqlitepath"));
            sqlitename.setText(properties.getProperty("sqlitename"));
            sqlitepass.setText(properties.getProperty("sqlitepass"));
            rowflag.setText(properties.getProperty("rowflag"));
            colflag.setText(properties.getProperty("colflag"));
            fieldnames.setText(properties.getProperty("fieldnames"));
        } catch (IOException e) {
            logger.error("读取属性文件错误");
        }
    }

    /**
     * 默认参数配置读取
     * @param e
     */
    @FXML
    public void updateConfig(ActionEvent e) {
        readConfig();
    }

    /**
     * 默认参数配置保存
     * @param e
     */
    @FXML
    public void saveConfig(ActionEvent e) {
        properties.setProperty("filepath", filepath.getText());
        properties.setProperty("sqlitepath", sqlitepath.getText());
        properties.setProperty("sqlitename", sqlitename.getText());
        properties.setProperty("sqlitepass", sqlitepass.getText());
        properties.setProperty("rowflag", rowflag.getText());
        properties.setProperty("colflag", colflag.getText());
        properties.setProperty("fieldnames", fieldnames.getText());
        try {
            logger.info("准备保存配置");
            File file = new File(propertyUrl);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter ow = new OutputStreamWriter(fos);
            properties.store(ow, "配置文件");
        } catch (IOException ex) {
            logger.error("读取属性文件错误");
        }
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
