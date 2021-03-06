[TOC]
## java基于freemark 的模板生成器
**说明：**
本工具是在windows平台下使用IDEA基于mysql与freemark 的代码生成工具的简单demo
在实际使用中请根据实际项目补充相关内容
### 1. 搭建java 项目
1. 在IDEA 新建maven工程
**构建maven项目结构**
- src
    * main
        *  java
             * tk.wioo 
               - DbDataUtils.java (获取数据库中表列的数据)
               - DBUtils.java  (数据库连接及sql语句执行)
               - FileUtils.java (文件备份及删除)
               - FreemarkUtils.java  (freemark相关操作)
               - GenMain.java (主类)
               - Model.java (数据库列的相关属性)
        *  resources
            * ftl  (模板存放位置)
                * model
                    - fpl.ftl
                    - frammemaker.ftl 
            - config.properties (主配置文件)
            - log4j.properties 
            - sql.properties (相关sql语句)
    * test
        *  java
        *  resources
2. 添加依赖
```xml
    <dependencies>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.17</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>1.7.25</version>
        </dependency>
        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>2.3.20</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>6.0.6</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.38</version>
        </dependency>
    </dependencies>
```

### 3. 设计配置文件
1. log4j.properties
```properties
log4j.rootLogger=debug,Console
log4j.appender.Console=org.apache.log4j.ConsoleAppender
log4j.appender.Console.layout=org.apache.log4j.PatternLayout
log4j.appender.Console.layout.ConversionPattern=[%c] - %m%n
```
2. sql.properties
```properties
mysql.select=select * from ${table}
#获取 所有的表
mysql.getAllTable=select TABLE_NAME from information_schema.tables where TABLE_SCHEMA=${dbName}
#获取表的字段及注释
mysql.getAllCol=select * from INFORMATION_SCHEMA.Columns where table_name=${table} and table_schema=${dbName}
```
3. config.properties
```properties
###数据源配
driverClassName=com.mysql.jdbc.Driver
url=jdbc:mysql://172.168.1.70:3306
username=root
password=root
dbName=demo3
dbType=mysql

#生成那些项目(与模板位置对应)
#genType=model,dao,dao.impl,mapper,service,service.impl,controller,junit
genType=model
#生成那些表
tables=user,users
#包名
package=com.wioo
###保存文件路径
fileSavePath=d://aaa
###备份文件夹
fileBakPath=bak
##项目名称
projectName=testDemo
## 使用模板名
#templateName=fpl
```

### 4. 数据库字段model
```java
public class Model {
    // 列名
    private String columnName;
    // 是否为空
    private String isNull;
    // 数据类型
    private String dataType;
    // 索引  PRI 主键 UNI 唯一索引 MUL索引
    private String columnKey;
    // 注释
    private String columnComment;
    // 最大
    private BigInteger characterMaxLength;

    set/get ...
}

```
### 5. 连接数据库
```java
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
```

### 6. 获取数据
```java
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
        map.put("TINYINT UNSIGNED   ","Integer");
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
}
```

### 7. 文件备份与删除

```java
public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static ResourceBundle getResource(String fileName) {
        ResourceBundle rb = ResourceBundle.getBundle(fileName, Locale.getDefault());
        BufferedInputStream inputStream;
        String proFilePath = System.getProperty("user.dir") + "\\" + fileName + ".properties";
        try {
            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
            rb = new PropertyResourceBundle(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            logger.warn("外部配置文件 :{} 没找到",(fileName + ".properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rb;
    }


    //删除文件和目录
    public static void clearFiles(String workspaceRootPath) {
        File file = new File(workspaceRootPath);
        if (file.exists()) {
            deleteFile(file);
        }
    }

    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            } else {
                file.delete();
            }
        }
        if (file.isFile()) {
            logger.info("文件删除成功 ：{}",file.getName());
            file.delete();
        }
    }

    //移动文件和目录
    public static void copyDir(String oldPath, String newPath) throws IOException {
        File file = new File(oldPath);
        String[] filePath = file.list();
        if (filePath != null && filePath.length > 0) {
            for (int i = 0; i < filePath.length; i++) {
                if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                    copyDir(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
                }
                if (new File(oldPath + file.separator + filePath[i]).isFile()) {
                    copyFile(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
                }
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        if (oldFile.exists()) {
            File file = new File(newPath);
            if (newPath.lastIndexOf(file.separator) > 0) {
                File newFlod = new File(newPath.substring(0, newPath.lastIndexOf(file.separator)));
                if (!newFlod.exists()) {
                    newFlod.mkdirs();
                }
            }
            oldFile.renameTo(file);
        }
    }
}
```


### 8. freemark 生成文件
```java
public class FreemarkUtils {
    static Logger logger= LoggerFactory.getLogger(FreemarkUtils.class);
    static ResourceBundle rb =FileUtils.getResource("config");
    // 获取模板
    public static Map<String, Template> getFtls() {
        Map<String, Template> result = new HashMap<String, Template>();
        try {
            String[] fileNames = rb.getString("genType").split(",");
            Configuration configuration = new Configuration();
            configuration.setClassForTemplateLoading(FreemarkUtils.class, "/ftl");
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8");   //页面乱码
            String templateName="";
            try {
                templateName = (rb.getString("templateName") != null && !"".equals(rb.getString("templateName"))) ?
                        ("/" + rb.getString("templateName") + ".ftl") : "/framemaker.ftl";
            }catch (Exception e){
                templateName = "/framemaker.ftl";
            }
            //获取或创建一个模版。
            for (String file : fileNames) {
                String ftlName = file.replace(".", "/");
                Template template = configuration.getTemplate(ftlName + templateName);
                result.put(ftlName, template);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    private static String typeToPackage(String sqlType){
        Map<String,String> map=new HashMap<String, String>();
        map.put("String","java.lang.String");
        map.put("byte[]","java.lang.byte[]");
        map.put("Long","java.lang.Long");
        map.put("Integer","java.lang.Integer");
        map.put("int","java.lang.Integer");
        map.put("Boolean","java.lang.Boolean");
        map.put("BigInteger","java.math.BigInteger");
        map.put("Float","java.lang.Float");
        map.put("Double","java.lang.Double");
        map.put("BigDecimal","java.math.BigDecimal");
        map.put("Date","java.sql.Date");
        map.put("Time","java.sql.Time");
        map.put("Timestamp","java.sql.Timestamp");
        return  map.get(sqlType);
    }
    private static String keyToPackage(String type){
        Map<String,String> map=new HashMap<String, String>();
        map.put("PRI","javax.persistence.Id");
        return  map.get(type);
    }


    public static Map<String, Object> mergeData(List<Model> models) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("models", models);
        List<String> dataType = new ArrayList<String>();
        for (Model model : models) {
//            String type=typeToPackage(model.getDataType());
//            if (!dataType.contains(type)) {
//                dataType.add(type);
//            }

            String columnKey=keyToPackage(model.getColumnKey());
            if (!dataType.contains(columnKey)) {
                dataType.add(columnKey);
            }

        }
        dataMap.put("importPackage", dataType);
        dataMap.put("projectName", rb.getString("projectName"));
        return dataMap;
    }

    public static String upperCase(String str) {
//        return str.substring(0, 1).toUpperCase() + str.substring(1);
        if (str.length() < 1)
            return "";
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    // 生成文件
    public static void makeFiles(String name, List<Model> models) {
        try {
            Map<String, Object> dataMap = mergeData(models);
            Map<String, Template> templates = getFtls();
            String targetFlod = rb.getString("fileSavePath") + "/";
            DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String bakFlod = targetFlod + "/" + rb.getString("fileBakPath") + "/" + df.format(new Date()) + "/";
            if (rb.getString("projectName") != null) {
                targetFlod += rb.getString("projectName");
                bakFlod += rb.getString("projectName");
            }
            // 备份文件
            FileUtils.copyDir(targetFlod, bakFlod);
            // 清空文件
            FileUtils.clearFiles(targetFlod);
            for (Map.Entry<String, Template> tpl : templates.entrySet()) {
                Template template = tpl.getValue();
                String packagePath = targetFlod;
                if (rb.getString("package") != null) {
                    packagePath = packagePath + "/" + rb.getString("package").replace(".", "/");
                    bakFlod += "/" + rb.getString("package").replace(".", "/") + "/" + tpl.getKey() + "/";
                }
                File writeFile = new File(packagePath + "/" + tpl.getKey());
                if (!writeFile.exists()) {
                    writeFile.mkdirs();
                }
                String fileName = upperCase(name) + upperCase((tpl.getKey().lastIndexOf("/") > -1 ? tpl.getKey().substring(tpl.getKey().lastIndexOf("/") + 1) : "")) ;
                String newFile = packagePath + "/" + tpl.getKey() + "/" + fileName+ ".html";
                Writer writer = new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8");
                dataMap.put("fileName",fileName);
                dataMap.put("package",  rb.getString("package") + "." + tpl.getKey());
                template.process(dataMap, writer);
            }
            logger.error("恭喜，生成成功~~");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }
}


```
### 7. Entity模板的编写示例(fpl.ftl)

```java
package ${package};

<#list importPackage as import>
    <#if import??>
import ${import!""};
    </#if>
</#list>

public class ${fileName} {

<#list models as model>
    <#if (model.columnKey=="PRI")>
    @Id
    </#if>
    private ${model.dataType!""}  ${model.columnName!""};
</#list>

<#list models as model>
    public ${model.dataType!""} get${model.columnName?cap_first}() {
        return ${model.columnName!""};
    }
    public void set${model.columnName?cap_first}(${model.dataType!""} ${model.columnName!""}) {
        this.${model.columnName!""} = ${model.columnName!""};
    }
</#list>

    @Override
    public String toString() {
        return "${fileName}{"
            <#list models as model>
            + "${model.columnName!""}=" + ${model.columnName!""}
            </#list>
                '}';
    }
}
```

### 8.打包（支持外部配置文件）
1. pom.xml文件
```xml
<build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>1.2.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>tk.wioo.GenMain</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
2. 打包:  mvn package
3. 运行（支持外部配置文件）:  java -jar wioo.jar  

<meta http-equiv="refresh" content="10">