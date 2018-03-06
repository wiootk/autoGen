package tk.wioo;

import java.math.BigInteger;

/**
 * Created by Administrator on 2017-09-28.
 */
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public BigInteger getCharacterMaxLength() {
        return characterMaxLength;
    }

    public void setCharacterMaxLength(BigInteger characterMaxLength) {
        this.characterMaxLength = characterMaxLength;
    }
}
