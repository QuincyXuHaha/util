package com.quincy.project.util.check;

import lombok.Data;

/**
 * 校验结果封装类
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
@Data
public class CheckResult {

    /**
     * true表示 校验通过
     */
    private boolean isSuccess;

    /**
     * 校验失败原因
     */
    private String errorMsg;

    private CheckResult() {
    }

    private CheckResult(boolean isSuccess, String errorMsg) {
        this.isSuccess = isSuccess;
        this.errorMsg = errorMsg;
    }

    public static CheckResult instance() {
        return new CheckResult(true, null);
    }

    public static CheckResult error(String errorMsg) {
        return new CheckResult(false, errorMsg);
    }


}
