package tk.wioo;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017-09-28.
 */
public class GenMain {
    public static void main(String[] args) {
        List<String> tableNames = DbDataUtils.getTables();
        Map<String, List<Model>> tables = DbDataUtils.getTableDatas(tableNames);
        for (Map.Entry<String, List<Model>> table : tables.entrySet()) {
            FreemarkUtils.makeFiles(table.getKey(), table.getValue());
        }
    }
}
