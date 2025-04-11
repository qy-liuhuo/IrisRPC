package io.github.qylh.iris.springdemo;

import io.github.qylh.iris.common.annotation.Api;
import io.github.qylh.iris.common.annotation.IrisService;
import org.springframework.stereotype.Component;

@IrisService(name = "TestService")
@Component
public class TestServiceImpl implements TestService {
    @Override
    @Api(name = "test")
    public String test(Integer a) {
        return "Hello, this is a test message from TestServiceImpl!" + a;
    }
}
