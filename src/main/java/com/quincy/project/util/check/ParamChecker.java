package com.quincy.project.util.check;

import com.quincy.project.util.StringUtils;
import com.quincy.project.util.check.annotation.NotEmpty;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 参数校验器
 *
 * @author xuguangquan
 * @date 2019/10/31 星期四
 */
@Slf4j
public class ParamChecker {


    public static CheckResult check(Object o) {
        Class<?> clazz = o.getClass();
        // 这样可以对父类属性进行校验
        while (clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                // 后续可扩展注解校验
                Annotation[] annotations = f.getAnnotations();
                for (Annotation ann : annotations) {
                    try {
                        Object value = f.get(o);
                        if (ann instanceof NotEmpty && StringUtils.isEmpty(value)) {
                            return CheckResult.error(((NotEmpty) ann).message());
                        }
                    } catch (IllegalAccessException e) {
                        log.error("程序对字段{}#{}进行非法访问", clazz.getName(), f.getName());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return CheckResult.instance();
    }

    public static void main(String[] args) {
        System.out.println(check(CheckResult.instance()));
    }

}
