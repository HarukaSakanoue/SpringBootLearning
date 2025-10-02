package com.example.todo.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    public List<TaskEntity> find(){
        var task1 = new TaskEntity(
            1,
            "SpringBootを学ぶ",
            "TODOアプリケーションを作ってみる",
            TaskStatus.TODO
        );
        var task2 = new TaskEntity(
            2,
            "SpringSecurityを学ぶ",
            "ログイン機能を作ってみる",
            TaskStatus.DOING
        );
        return List.of(task1,task2);



    }
}
