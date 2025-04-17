package io.github.qylh.iris.example.clientspring;


import io.github.qylh.iris.core.annotation.IrisRPC;
import io.github.qylh.iris.example.common.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{

    @IrisRPC
    private TestService testService;

    @GetMapping("/test")
    public String test() {
        System.out.println("12345");
        return testService.toString();
    }

}
