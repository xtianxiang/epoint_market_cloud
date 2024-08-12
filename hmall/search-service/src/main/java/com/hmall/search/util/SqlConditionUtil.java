package com.hmall.search.util;

import cn.hutool.core.util.StrUtil;
import org.mockito.internal.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xu
 * @version 1.0
 * @date 2024/7/16 12:05
 * @DESCRIPTION
 */
public class SqlConditionUtil {
    private Map<String, String> conditionMap = new LinkedHashMap<String, String>();
    public void equal(String fieldName, Object fieldValue) {
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        }
        conditionMap.put(fieldName + "=", String.valueOf(fieldValue));
    }
    /**
     *
     * 不等于
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     */
    public void notEqual(String fieldName, Object fieldValue) {
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        }
        conditionMap.put(fieldName + "<>", String.valueOf(fieldValue));
    }
    /**
     *
     * 大于
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     * @param isEqual
     *            能否等于
     */
    public void greatThan(String fieldName, Object fieldValue, boolean isEqual) {
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        }
        if (isEqual) {
            conditionMap.put(fieldName + ">=", String.valueOf(fieldValue));
        }
        else {
            conditionMap.put(fieldName + ">", String.valueOf(fieldValue));
        }

    }
    /**
     *
     * 小于
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     * @param isEqual
     *            能否等于
     */
    public void lessThan(String fieldName, Object fieldValue, boolean isEqual) {
        if (fieldValue instanceof String) {
            fieldValue = "'" + fieldValue + "'";
        }
        if (isEqual) {
            conditionMap.put(fieldName + "<=", String.valueOf(fieldValue));
        }
        else {
            conditionMap.put(fieldName + "<", String.valueOf(fieldValue));
        }

    }


    /**
     *
     * like查询
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     */
    public void columnLike(String fieldName, String fieldValue) {
        if (fieldValue.startsWith("%") || fieldValue.endsWith("%")) {
            fieldValue = "'" + fieldValue + "'";
        }
        else {
            fieldValue = "'%" + fieldValue + "%'";
        }
        conditionMap.put(fieldName + " LIKE", fieldValue);
    }
    /**
     *
     * in查询
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     */
    public void columnIn(String fieldName, String fieldValue) {
        if (StrUtil.isNotBlank(fieldValue)) {
            String inStr = "";
            StringBuilder inStrBld = new StringBuilder();
            String[] valueArray = fieldValue.split(",");
            if (valueArray != null && valueArray.length > 0) {
                for (String value : valueArray) {
                    inStrBld.append("'" + value + "',");
                }
                inStr += inStrBld.toString();
                if (inStr.length() > 0) {
                    inStr = inStr.substring(0, inStr.length() - 1);
                }
            }
            conditionMap.put(fieldName + " IN", inStr);
        }
    }

    /**
     *
     * notin查询
     *
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     */
    public void columnNotIn(String fieldName, String fieldValue) {
        if (StrUtil.isNotBlank(fieldValue)) {
            String inStr = "";
            // inStr = fieldValue.replace(",", "','");
            StringBuilder inStrBld = new StringBuilder();
            String[] valueArray = fieldValue.split(",");
            if (valueArray != null && valueArray.length > 0) {
                for (String value : valueArray) {
                    inStrBld.append("'" + value + "',");
                }
                inStr += inStrBld.toString();
                if (inStr.length() > 0) {
                    inStr = inStr.substring(0, inStr.length() - 1);
                }
            }
            conditionMap.put(fieldName + " NOT IN", inStr);
        }
    }

}
