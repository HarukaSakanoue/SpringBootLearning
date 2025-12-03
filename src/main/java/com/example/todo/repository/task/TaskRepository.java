package com.example.todo.repository.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskSearchEntity;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TaskRepository {

  @Select("""
    <script>
      SELECT id, summary, description, status
      FROM tasks
      <where>
        <if test="condition.summary != null and condition.summary != ''">
          AND summary LIKE CONCAT('%', #{condition.summary}, '%')
        </if>
        <if test="condition.status != null and condition.status.size() &gt; 0">
          AND status IN
          <foreach item="item" collection="condition.status" open="(" close=")" separator=",">
            #{item}
          </foreach>
        </if>
      </where>
    </script>
    """)
  List<TaskEntity> select(@Param("condition") TaskSearchEntity condition);


  @Select("SELECT id, summary, description, status FROM tasks WHERE id = #{taskId};")
  Optional<TaskEntity> selectById(@Param("taskId") long taskId);

  /**
   * タスクを挿入
   * 
   * 注意: recordは不変なので、自動生成されたIDは取得できません
   * TaskService側で再取得する必要があります
   */
  @Insert("INSERT INTO tasks (summary, description, status) VALUES (#{task.summary}, #{task.description}, #{task.status})")
  void insert(@Param("task") TaskEntity newEntity);
  
  /**
   * 最後に挿入されたタスクのIDを取得(H2データベース用)
   * H2 2.x系では関数が限定的なため、MAX(id)を使用
   */
  @Select("SELECT MAX(id) FROM tasks")
  Long selectMaxId();

  @Update("UPDATE tasks SET summary = #{task.summary}, description = #{task.description}, status = #{task.status} WHERE id = #{task.id}")
  void update(@Param("task") TaskEntity entity);

  @Update("DELETE FROM tasks WHERE id = #{taskId}")
  void delete(@Param("taskId") long id);

}
