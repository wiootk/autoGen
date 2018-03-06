package tk.wioo;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017-09-28.
 */
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
