package io.github.qylh.iris.demo;

import io.github.qylh.iris.common.annotation.Api;
import io.github.qylh.iris.common.annotation.IrisService;

@IrisService(name = "TestService")
public class TestServiceImpl implements TestService {
    @Override
    @Api(name = "test")
    public String test(Integer a) {
        return "Hello, this is a test message from TestServiceImpl!" + a;
    }
}
