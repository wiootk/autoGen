package tk.wioo;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * Created by Administrator on 2017-09-28.
 */
public class DBUtils {
    private static Logger logger = LoggerFactory.getLogger(DBUtils.class);
    public static Connection getConnection() {
        ResourceBundle rb =FileUtils.getResource("config");
//        ResourceBundle rb = ResourceBundle.getBundle("config", Locale.getDefault());
        StringBuilder preUrl = new StringBuilder();
        preUrl.append(rb.getString("url")).append("/").append(rb.getString("dbName"))
                .append("?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC");
        String url = preUrl.toString();
        String username = rb.getString("username");
        String password = rb.getString("password");
        try {
            Connection con = DriverManager.getConnection(url, username, password);
            return con;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return null;
    }

    public static List executeSql(String sql) {
        Connection con = getConnection();
        List list = new ArrayList();
        if (con == null) {
            return new ArrayList();
        }
        try {
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
            int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
            Map rowData = new HashMap();
            while (rs.next()) {
                rowData = new HashMap(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(rowData);
            }
            rs.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("数据库数据成功获取！！");
            return list;
        }
    }
}
