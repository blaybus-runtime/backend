package com.blaybus.backend.domain.content.repository;

import com.blaybus.backend.domain.content.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todoTask.id = :taskId ORDER BY c.createdAt ASC")
    List<Comment> findByTaskId(@Param("taskId") Long taskId);
}
