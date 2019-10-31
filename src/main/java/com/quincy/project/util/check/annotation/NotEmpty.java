package com.quincy.project.util.check.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 申明这个字段不能为空
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
@Documented
@Target({FIELD})
@Retention(RUNTIME)
public @interface NotEmpty {

    String message() default "参数不能为空";

}
