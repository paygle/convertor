package com.dodar.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.dodar.utils.Log.logger;

public class SQLite {

    /**
     * 连接对象
     */
    private static Connection connection;

    /**
     * SQL语句处理对象
     */
    public static Statement statement;

    /**
     * 数据库检测信息
     */
    public static Map<String, Object> databaseInfo = new HashMap<>();

    /**
     * 初始化数据库对象
     * @param url
     * @param usr
     * @param pwd
     */
    public static void init(String url, String usr, String pwd) {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                logger.error("加载 Sqlite 驱动失败");
                ex.printStackTrace();
            }
            try {
                url = "jdbc:sqlite:" + url;
                connection = DriverManager.getConnection(url, usr, pwd);
                statement = connection.createStatement();
                logger.info("数据库连接成功");
            } catch (SQLException ex) {
                logger.error("数据库连接失败");
                ex.printStackTrace();
            }

        } else if (connection instanceof Connection && statement == null) {
            try {
                statement = connection.createStatement();
            } catch (SQLException ex) {
                logger.error("数据库语句对象创建失败");
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取数据库语言操作对象
     * @return
     */
    public static Statement getStatement() {
        if (statement == null) logger.error("数据库连接未初始");
        return statement;
    }

    /**
     * 单条语句查询
     * @param sql
     * @return
     */
    public static ResultSet query(String sql) {
        ResultSet resultSet = null;
        if (statement instanceof  Statement) {
            try {
                resultSet = statement.executeQuery(sql);
            } catch (SQLException ex) {
                logger.error("SQLite 查询错误：");
                ex.printStackTrace();
            }
        } else {
            logger.error("数据库连接未初始");
        }
        return resultSet;
    }

    public static boolean checkDatabaseInfo(String tablename) {
        boolean isOk = false;
        if (databaseInfo.get(tablename) == null) {
            Map<String, Object> infos = new HashMap<>();
             // 查询全部表名
            ResultSet rsTns = query("SELECT name FROM sqlite_master");
            ArrayList<String> tablenames = new ArrayList<>();
            try {
                while (true) {
                    if (!rsTns.next()) break;
                    tablenames.add(rsTns.getString("name"));
                }
                rsTns.close();
            } catch (SQLException e) {
                logger.error("获取表名错误");
                e.printStackTrace();
            }
            infos.put("tableNames", tablenames);
            // 查询对应表名的全部字段
            ResultSet rsTinfo = query("PRAGMA table_info("+tablename+")");
            ArrayList<String> tbfields = new ArrayList<>();
            try {
                while (true) {
                    if (!rsTinfo.next()) break;
                    tbfields.add(rsTinfo.getString("name"));
                }
                rsTinfo.close();
            } catch (SQLException e) {
                logger.error("获取表名字段错误");
                e.printStackTrace();
            }

            infos.put("fieldsInfo", tbfields);
            databaseInfo.put(tablename, infos);
            isOk = true;
        } else if (databaseInfo.get(tablename) instanceof HashMap) {
            isOk = true;
        }
        return isOk;
    }

    /**
     * 内部使用创建表用
     * @return
     */
    private static int creatTable(String createSql, String table) {
        int ret = 0;
        try {
            ret = statement.executeUpdate(createSql);
            ret = 1;
        } catch (SQLException e) {
            logger.error("创建 " + table + " 表错误");
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 自动更新语句 INSERT, UPDATE
     * @param table   例如： tablename
     * @param feilds  例如： f1,f2,f3,...
     * @param values  例如： v1,v2,v3,...
     * @param key     唯一值字段名称
     * @return
     */
    public static int replace(String table, String feilds, String values, String key) {
        int ret = 0;
        feilds = feilds.replaceAll("\\s*", "");
        String[] fds = feilds.split(",");
        String[] vals = values.split(",");
        String createSql = null;
        String insertSql = null;
        String updateSql = null;
        StringBuffer keyval = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        StringBuffer sbv = new StringBuffer();
        StringBuffer csb = new StringBuffer();

        // SQL语句拼接
        if (fds.length > 0 && fds.length == vals.length) {
            for (int i = 0; i < fds.length; i++) {
                if (fds.length == 1 || i == fds.length - 1) {
                    sb.append(" " + fds[i] +"=\'" + vals[i] + "\'");
                    sbv.append("\'" + vals[i] + "\'");
                } else {
                    sb.append(" " + fds[i] +"=\'" + vals[i] + "\',");
                    sbv.append("\'" + vals[i] + "\',");
                }
                if (key.equals(fds[i])) {
                    keyval.append("\'" + vals[i] + "\'");
                }
                if (i == 0) {
                    csb.append("\'" + fds[i] + "\' TEXT NOT NULL,");
                } else {
                    csb.append("\'" + fds[i] + "\' TEXT,");
                }
            }
            insertSql = "INSERT INTO " + table + " (" + feilds+ ") " + "VALUES (" + sbv.toString() + ")";
            updateSql = "UPDATE " + table + " SET " + sb.toString() + " WHERE " + key + "=" + keyval.toString();
            createSql = "CREATE TABLE \'"+ table + "\' ("+ csb.toString() + " PRIMARY KEY (\'"+fds[0]+"\'))";
        } else {
            logger.error("数据字段与值对应不一致");
            return 0;
        }

        // 保证数据库表存在，字段一致
        if (checkDatabaseInfo(table)) {
            HashMap dbKeyinfo = (HashMap) databaseInfo.get(table);
            if (dbKeyinfo != null) {
                ArrayList tableNames = (ArrayList) dbKeyinfo.get("tableNames");
                // 表名判断， 创建一个新表
                if (tableNames == null || (tableNames != null && tableNames.indexOf(table) < 0)) {
                    if (creatTable(createSql, table) > 0) {
                        logger.info("表名：" + table + "创建成功");
                        checkDatabaseInfo(table);
                        dbKeyinfo = (HashMap) databaseInfo.get(table);
                    }
                }
                ArrayList fieldsInfo = (ArrayList) dbKeyinfo.get("fieldsInfo");
                // 表结构判断
                if (fieldsInfo != null) {
                    for (int j = 0; j <fds.length; j++) {
                        if (fieldsInfo.indexOf(fds[j]) < 0) {
                            logger.error("表名：" + table + "已经存在，但结构不一致");
                            return 0;
                        }
                    }
                }
            } else {
                // 找不到对应的表信息，直接创建表
                creatTable(createSql, table);
                checkDatabaseInfo(table);
            }
        } else {
            logger.error("检测数据库基础信息错误");
            return 0;
        }

        // SQL语句执行
        try{
            ret = statement.executeUpdate(updateSql);
            if (ret < 1) {
                logger.info("update failed：" + key + "=" + keyval);
                ret = statement.executeUpdate(insertSql);
                if (ret > 0) logger.info("insert success：" + key + "=" + keyval);
            } else {
                logger.info("update success：" + key + "=" + keyval);
            }
        } catch (SQLException ex) {
            logger.info("更新失败：" + key + "=" + keyval);
            ex.printStackTrace();
        }
        return ret;
    }

    /**
     * 添加批处理SQL
     * @param sql
     */
    public static void addBatch(String sql) {
        if (statement instanceof  Statement) {
            try {
                statement.addBatch(sql);
            } catch (SQLException ex) {
                logger.error("添加批处理SQL错误");
                ex.printStackTrace();
            }
        }
    }

    /**
     * 执行批处理
     * @return
     */
    public static int[] executeBatch() {
        int[] ret = null;
        if (statement instanceof Statement) {
            try {
                ret = statement.executeBatch();
            } catch (SQLException ex) {
                logger.error("批处理失败");
                ex.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 关闭窗口清理
     */
    public static void closeAll() {
        try {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
            logger.info("数据库释放成功");
        } catch (SQLException ex) {
            logger.info("数据库释放失败");
            ex.printStackTrace();
        }
    }


}
