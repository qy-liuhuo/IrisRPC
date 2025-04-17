package io.github.qylh.iris.example.server;

import io.github.qylh.iris.core.annotation.Api;
import io.github.qylh.iris.core.annotation.IrisService;
import io.github.qylh.iris.example.common.TestService;

@IrisService(name = "TestService")
public class TestServiceImpl implements TestService {
    @Override
    @Api(name = "test")
    public String test(Integer a) {
        return "Hello, this is a test message from TestServiceImpl!" + a;
    }
}
