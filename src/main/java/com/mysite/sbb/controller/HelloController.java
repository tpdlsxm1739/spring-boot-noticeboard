package com.mysite.sbb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller  //HelloController 이 에너테이션이 있어야 스프링부트 프레임워크가 컨트롤러로 인식한다.
public class HelloController {

    @GetMapping("/hello") //URL요청이 발생하면 HELLO메서드가 실행됨을 의미한다.즉 URL과 hello메서드를 매핑하는 역할을 한다
    @ResponseBody //hello 메서드의 응답 결과가 문자열 그 자체임을 나타낸다.
    //URL 요청에 대한 응답으로 문자열을 리턴하라는 의미이다.
    public String Hello(){
        return "Hello Spring B광범 ";
    }

}
