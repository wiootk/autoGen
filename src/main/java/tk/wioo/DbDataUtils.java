package tk.wioo;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Administrator on 2017-09-28.
 */
public class DbDataUtils {
//    static ResourceBundle config = ResourceBundle.getBundle("config", Locale.getDefault());
//    static ResourceBundle sql = ResourceBundle.getBundle("sql", Locale.getDefault());
    static ResourceBundle config =FileUtils.getResource("config");
    static ResourceBundle sql =FileUtils.getResource("sql");
    public static List<String> getTables() {
        List<String> tables = new ArrayList<String>();
        List<String> allTables = new ArrayList<String>();
        String sqlGetTable = sql.getString(config.getString("dbType") + ".getAllTable").replace("${dbName}", "'" + config.getString("dbName") + "'");
        List<Map<String, String>> result = (List<Map<String, String>>) DBUtils.executeSql(sqlGetTable);
        for (Map<String, String> map : result) {
            allTables.add(map.get("TABLE_NAME"));
        }
        if (config.getString("tables") != null && !"".equals(config.getString("tables"))) {
            List<String> tempTables = Arrays.asList(config.getString("tables").split(","));
            for (String tem : tempTables) {
                if (allTables.contains(tem)) {
                    tables.add(tem);
                }
            }
        } else {
            tables.addAll(allTables);
        }
        return tables;
    }

    public static Map<String, List<Model>> getTableDatas(List<String> tables) {
        Map<String, List<Model>> tableAndColnums=new HashMap<String, List<Model>>();
        for(String table:tables){
            List<Model> colnums=getDatas(table);
            tableAndColnums.put(table,colnums);
        }
        return tableAndColnums;
    }

    private static List<Model> getDatas(String tableName) {
        List<Model> columnList=new ArrayList<Model>();
        String sqlGetTable = sql.getString(config.getString("dbType") + ".getAllCol").replace("${dbName}", "'" + config.getString("dbName") + "'")
                .replace("${table}", "'" + tableName+ "'");
        List<Map<String, Object>> result = (List<Map<String, Object>>) DBUtils.executeSql(sqlGetTable);
        for(Map<String, Object> map:result){
            Model model=new Model();
            model.setColumnName((String) map.get("COLUMN_NAME"));
            model.setIsNull((String)map.get("IS_NULLABLE"));
            model.setDataType(converToJavaType(((String)map.get("DATA_TYPE")).toUpperCase()));
            model.setColumnKey((String)map.get("COLUMN_KEY"));
            model.setColumnComment((String)map.get("COLUMN_COMMENT"));
            model.setCharacterMaxLength((BigInteger)map.get("CHARACTER_MAXIMUM_LENGTH"));
            columnList.add(model);
        }
        return columnList;
    }

    private static String converToJavaType(String sqlType){
        Map<String,String> map=new HashMap<String, String>();
        map.put("VARCHAR","String");
        map.put("CHAR","String");
        map.put("BLOB","byte[]");
        map.put("INTEGER UNSIGNED","Long");
        map.put("PK (INTEGER UNSIGNED)","Long");
        map.put("INT","int");
        map.put("TINYINT UNSIGNED	","Integer");
        map.put("BOOLEAN","Integer");
        map.put("SMALLINT UNSIGNED","Integer");
        map.put("MEDIUMINT UNSIGNED","Integer");
        map.put("BIT","Boolean");
        map.put("BIGINT UNSIGNED","BigInteger");
        map.put("FLOAT","Float");
        map.put("DOUBLE","Double");
        map.put("DECIMAL","BigDecimal");
        map.put("DATE","Date");
        map.put("TIME","Time");
        map.put("DATETIME","Timestamp");
        map.put("TIMESTAMP","Timestamp");
        map.put("YEAR","Date");
        return  map.get(sqlType);
    }





    public static void main(String[] args) {
//        DbDataUtils.getTables();
        DbDataUtils.getTableDatas(DbDataUtils.getTables());
    }


}
