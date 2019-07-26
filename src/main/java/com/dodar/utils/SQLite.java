package com.dodar.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.dodar.utils.Log.logger;

public class SQLite {

    private static Connection connection;

    private static Statement statement;

    /**
     * 获取数据库语言操作对象
     * @param url
     * @param uname
     * @param pwd
     * @return
     */
    public static Statement getStatement(String url, String uname, String pwd) {

        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                logger.error("加载 Sqlite 驱动失败");
                ex.printStackTrace();
            }
            try {
                url = "jdbc:sqlite:" + url;
                connection = DriverManager.getConnection(url, uname, pwd);
                statement = connection.createStatement();
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

        return statement;
    }
}
