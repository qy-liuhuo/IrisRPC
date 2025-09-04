package io.github.qylh.iris.spring.boot.annotation;

import io.github.qylh.iris.spring.boot.IrisMCPToolRegister;
import io.github.qylh.iris.spring.boot.IrisReferenceBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({IrisReferenceBeanPostProcessor.class, IrisMCPToolRegister.class})
public @interface EnableIrisClient {
}
