package io.github.qylh.iris.example.serverspring;

import io.github.qylh.iris.core.annotation.Api;
import io.github.qylh.iris.core.annotation.IrisService;
import io.github.qylh.iris.example.common.TestService;
import org.springframework.stereotype.Service;

@IrisService(name = "TestService")
@Service
public class TestServiceImpl implements TestService {
    @Override
    @Api(name = "test")
    public String test(Integer a) {
        return "Hello, this is a test message from TestServiceImpl!" + a;
    }
}
