package com.example.todo.service.task;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.repository.task.TaskRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;


    public List<TaskEntity> find(TaskSearchEntity searchEntity) {
        return taskRepository.select(searchEntity);
    }


    public Optional<TaskEntity> findById(long taskId) {
        return taskRepository.selectById(taskId);
    }

    @Transactional
    public TaskEntity create(TaskEntity newEntity) {
        // タスクを挿入
        taskRepository.insert(newEntity);
        // 挿入直後のIDを取得(H2データベースの場合、MAX(id)を使用)
        Long generatedId = taskRepository.selectMaxId();
        // 生成されたIDでエンティティを再取得
        return taskRepository.selectById(generatedId).orElse(newEntity);
    }

    @Transactional
    public TaskEntity update(TaskEntity entity) {
        taskRepository.update(entity);
        // 更新後のエンティティを再取得
        return taskRepository.selectById(entity.id()).orElse(entity);
    }

    @Transactional
    public void delete(long id) {
        taskRepository.delete(id);
    }
}
