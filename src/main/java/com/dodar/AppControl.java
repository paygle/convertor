package com.dodar;

import com.dodar.utils.SQLite;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.sql.Statement;
import java.util.*;

import static com.dodar.utils.Log.logger;

public class AppControl implements Initializable {

    /**
     * 属性配置对象
     */
    private Properties properties;
    private String propertyUrl;
    private Statement statement;
    /**
     * 作为组件之间通信的承载对象
     */
    private Stage stage;

    /**
     * 文件扩展名
     */
    private String filext = ".txt";

    /**
     * 数据库字段名集合
     */
    private String[] fields;

    /**
     * 读取文件转换后的数据
     */
    private ArrayList<String[]> filedata = new ArrayList<>();

    /**
     * 根据字段动态自动的数据对象集合
     */
    private ArrayList<Object> beansdata = new ArrayList<>();

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
    /**
     * 状态信息栏
     */
    @FXML
    private Label loginfo;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

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
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        String path = file.getPath();
        sqlitepath.setText(path);
    }

    /**
     * 初始化数据库字段集合
     */
    public void initFieldKeys() {
        String fnames = properties.getProperty("fieldnames");
        if (!fnames.isEmpty()) {
            fields = fnames.split(",");
        }
    }

    /**
     * 读取配置文件
     */
    public void readConfig() {
        File file = new File(propertyUrl);
        if (file.exists()) {
            try {
                logger.info("准备读取配置");
                loginfo.setText("读取配置");
                FileInputStream inputStream = new FileInputStream(file);
                properties.load(inputStream);
                filepath.setText(properties.getProperty("filepath"));
                sqlitepath.setText(properties.getProperty("sqlitepath"));
                sqlitename.setText(properties.getProperty("sqlitename"));
                sqlitepass.setText(properties.getProperty("sqlitepass"));
                rowflag.setText(properties.getProperty("rowflag"));
                colflag.setText(properties.getProperty("colflag"));
                fieldnames.setText(properties.getProperty("fieldnames"));
                initFieldKeys();
            } catch (IOException e) {
                loginfo.setText("读取配置错误");
                logger.error("读取属性文件错误");
            }
        }
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
        initFieldKeys();
        try {
            loginfo.setText("保存配置");
            logger.info("准备保存配置");
            File file = new File(propertyUrl);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter ow = new OutputStreamWriter(fos);
            properties.store(ow, "配置文件");
        } catch (IOException ex) {
            loginfo.setText("读取属性文件错误");
            logger.error("读取属性文件错误");
        }
    }

    /**
     * 读取单个数据文件转成字符串
     * @return
     */
    public String readFile(File file) {
        String encoding = "UTF-8";
        FileInputStream inputStream;
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(filecontent);
            return new String(filecontent, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将从文件中读取出来的字符串数据转换成列表对象数据
     * @param data
     * @return
     */
    public ArrayList<String[]> fileStrToData(String data) {
        String strdata = data.replaceAll("[\\t\\n\\r]", "");
        String rowsplit = rowflag.getText();
        String colsplit = colflag.getText();
        String[] rowsdata = strdata.split(rowsplit);
        ArrayList<String[]> tmplist = new ArrayList<>();
        if (rowsplit.isEmpty() || colsplit.isEmpty()) {
            loginfo.setText("行列分隔符缺失");
            return null;
        } else {
            for (int i = 0; i < rowsdata.length; i++) {
                String[] colsdata = rowsdata[i].split(colsplit);
                if (colsdata.length > 0) {
                    tmplist.add(colsdata);
                }
            }
            return tmplist;
        }
    }

    /**
     * 读取TEXT数据文件并转换成数据库对象
     * @param e
     */
    @FXML
    public void readFilesToData(ActionEvent e) {
        String path;
        ArrayList<String[]> tmpdata;
        String dirpath = filepath.getText();
        File file = new File(dirpath);
        File[] flist = file.listFiles();
        Map<String, Object> map = new HashMap<>();

        if (flist != null) {
            loginfo.setText("读取数据文件...");
            logger.info("开始读取数据文件");
            // 读取所有数据文件且合并数据
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isFile()) {
                    path = flist[i].getPath();
                    if (path.lastIndexOf(filext) >= 0) {
                        tmpdata = fileStrToData(readFile(flist[i]));
                        if (tmpdata instanceof ArrayList) {
                            filedata.addAll(tmpdata);
                        }
                    }
                }
            }

            // 动态生成数据对象
            if (filedata.size() > 0 && fields instanceof String[]) {
                for (String[] tmpstr : filedata) {
                    try {
                        for (int j = 0; j < fields.length; j++) {
                            if (fields[j] instanceof String) {
                                if (tmpstr[j] instanceof String) {
                                    loginfo.setText(fields[j] + "=" + tmpstr[j]);
                                    map.put(fields[j], tmpstr[j]);
                                } else {
                                    map.put(fields[j], "");
                                }
                            }
                        }

                    } catch (Exception ex) {
                        loginfo.setText("动态生成数据类错误");
                        logger.error("动态生成数据类错误" + ex);
                    }
                }
            } else {
                loginfo.setText("数据字段未设置");
            }

            if (beansdata.size() > 0) {
                setTableViewData(beansdata);
            }
        } else {
            loginfo.setText("没有选择读取数据的目录");
            logger.error("没有选择读取数据的目录");
        }
    }

    /**
     * 初始化SQLite 语句对象
     */
    public void initStatement() {
        String sqlpath = sqlitepath.getText();
        if (statement == null && !sqlpath.isEmpty()) {
            String sqlname = sqlitename.getText();
            String sqlpass = sqlitepass.getText();
            SQLite.init(sqlpath, sqlname, sqlpass);
            statement = SQLite.statement;
        }
    }

    /**
     * 设置 TableView 显示数据
     * @param dataObject
     */
    public void setTableViewData(ArrayList<Object> dataObject) {
        initStatement();
        if (statement != null) {

        }
    }

    /**
     * 保存数据到Sqlite数据库
     */
    public void saveDataSqlite() {
        initStatement();
        if (statement != null) {
            SQLite.replace("example", "name, content", "cc,cccccc", "name");
        }
    }

    public void closeConnection() {
        SQLite.closeAll();
    }
}
