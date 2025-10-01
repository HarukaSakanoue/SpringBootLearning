package com.example.todo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class IndexController{

    //http:localhost:8080/ -. "Hello,world!
    //Get /-> *Hello,Workd!
    //no usages new*
    @GetMapping
    @ResponseBody
    public String index(){
        return "Hello,World!";
    }

}
