package com.example.newcode.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
	// 运行在方法上，这个注解仅仅是用作一个表示，根本目的是为了LoginRequiredInterceptor拦截器可以找到哪些访问接口API需要这种
}
