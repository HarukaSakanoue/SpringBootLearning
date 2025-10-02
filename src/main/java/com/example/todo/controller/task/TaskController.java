package com.example.todo.controller.task;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.util.List;


@Controller
public class TaskController {

    @GetMapping("/tasks")
    public String List(Model model){
        var task1 = new TaskDTO(
            1, 
            "SpringBootを学ぶ", 
            "TODOアプリケーションを作ってみる", 
            "TODO"
        );

        var task2 = new TaskDTO(
            2, 
            "SpringSecurityを学ぶ", 
            "ログイン機能を作ってみる", 
            "TODO"
        );
        var taskList = List.of(task1,task2);
        
        model.addAttribute("taskList", taskList);
        return "tasks/list";
    }
}