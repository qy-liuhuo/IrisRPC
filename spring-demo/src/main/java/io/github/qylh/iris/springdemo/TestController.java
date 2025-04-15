package io.github.qylh.iris.springdemo;


import io.github.qylh.iris.common.annotation.IrisRPC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{

    @IrisRPC
    private TestService testService;

    @GetMapping("/test")
    public String test() {
        return testService.test(1);
    }

}
