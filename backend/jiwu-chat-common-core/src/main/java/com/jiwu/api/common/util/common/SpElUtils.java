package com.jiwu.api.common.util.common;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * SpEL表达式解析工具类
 */
public class SpElUtils {
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 解析SpEL表达式
     *
     * @param spEl  SpEL表达式
     * @param method 方法
     * @param args   参数值
     * @return 解析后的字符串
     */
    public static String parse(String spEl, Method method, Object[] args) {
        String[] params = parameterNameDiscoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], args[i]);
            }
        }
        Expression expression = parser.parseExpression(spEl);
        return expression.getValue(context, String.class);
    }
}
