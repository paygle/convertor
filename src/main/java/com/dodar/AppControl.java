package com.dodar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

import static com.dodar.utils.Log.logger;

public class AppControl implements Initializable {

    /**
     * 属性配置对象
     */
    private Properties properties;
    private String propertyUrl;
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
     * 获取数据库字段集合
     * @param keys
     * @return
     */
    public String[] getFieldKeys(String keys) {
        return keys.split(",");
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
            String fnames = properties.getProperty("fieldnames");
            filepath.setText(properties.getProperty("filepath"));
            sqlitepath.setText(properties.getProperty("sqlitepath"));
            sqlitename.setText(properties.getProperty("sqlitename"));
            sqlitepass.setText(properties.getProperty("sqlitepass"));
            rowflag.setText(properties.getProperty("rowflag"));
            colflag.setText(properties.getProperty("colflag"));
            fieldnames.setText(fnames);
            if (fnames instanceof String) fields = getFieldKeys(fnames);
        } catch (IOException e) {
            logger.error("读取属性文件错误");
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

        for (int i = 0; i < rowsdata.length; i++) {
            String[] colsdata = rowsdata[i].split(colsplit);
            if (colsdata.length > 0) {
                tmplist.add(colsdata);
            }
        }
        return tmplist;
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
        if (filedata.size() > 0 && fields instanceof String[] && fields.length > 0) {
            Map<String, Object> map = new HashMap<>();
            for (String[] tmpstr : filedata) {

                try {
                    for (int j = 0; j < fields.length; j++) {
                        if (fields[j] instanceof String) {
                            if (tmpstr[j] instanceof String) {
                                map.put(fields[j], tmpstr[j]);
                            } else {
                                map.put(fields[j], "");
                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.error("动态生成数据类错误" + ex);
                }
            }
        }

        if (beansdata.size() > 0) {
            setTableViewData(beansdata);
        }
    }

    /**
     * 设置 TableView 显示数据
     * @param dataObject
     */
    public void setTableViewData(ArrayList<Object> dataObject) {


    }

    /**
     * 保存数据到Sqlite数据库
     * @param e
     */
    @FXML
    public void saveDataSqlite(ActionEvent e) {

    }

}
