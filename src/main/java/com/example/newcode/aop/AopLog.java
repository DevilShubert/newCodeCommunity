package com.example.newcode.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Aspect
// @Component
@Slf4j
public class AopLog {

    @Pointcut("execution(public  * com.example.newcode.controller.*.*(..))")
    public void ControllerLog(){}

    @Before("ControllerLog()")
    public void doBeforeController(JoinPoint point){
        // 这个方法是从Servlet中都有的（JavaEE规范从而确保每个请求/对应的线程都必须有这个参数）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
        log.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }


    @Pointcut("execution(public  * com.example.newcode.controller.advice.*.*(..))")
    public void ErrorLog(){

    }


    @Before("ErrorLog()")
    public void doBeforeError(JoinPoint point){
        // 这个方法是从Servlet中都有的（JavaEE规范从而确保每个请求/对应的线程都必须有这个参数）
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
        log.error(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }


}
