package com.example.todo.controller.task;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.todo.service.task.TaskService;

import lombok.RequiredArgsConstructor;

import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;


    @GetMapping("/tasks")
    public String List(Model model){
        var taskList = taskService.find()
            .stream()
            .map(entity -> TaskDTO.toDTO(entity)
            )
            .toList();

        model.addAttribute("taskList", taskList);
        return "tasks/list";
    }


@GetMapping("/tasks/{id}")
    public String showDetail(@PathVariable("id") long taskId, Model model){
        
        var taskEntity = taskService.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found : Id =" + taskId));
        model.addAttribute("task", TaskDTO.toDTO(taskEntity));
        return "tasks/detail";
    }
}