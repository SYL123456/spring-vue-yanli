package com.example.demo.client;

import com.example.demo.entity.AttitudeResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "python-service",url = "http://127.0.0.1:80")
@Component
public interface PythonClient {

    @RequestMapping(value = "/ner",method = RequestMethod.GET,headers = {"Content-Type=application/json;charset=utf-8","Accept=application/json"})
    AttitudeResp getPythonResult(@RequestParam("text") String text);

}
