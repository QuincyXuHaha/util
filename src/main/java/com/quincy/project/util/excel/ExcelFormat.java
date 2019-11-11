package com.quincy.project.util.excel;

/**
 * excel 数据格式
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
public enum ExcelFormat {

    FORMAT_INTEGER("INTEGER"),
    FORMAT_DOUBLE("DOUBLE"),
    FORMAT_PERCENT("PERCENT"),
    FORMAT_DATE("DATE");

    private final String value;

    ExcelFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
