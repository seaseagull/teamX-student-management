
package com.teach.javafx.models;

import java.time.LocalDateTime;

// 考试实体类 匹配后端字段
public class Exam {
    // 后端字段完全一致
    private Long examId;
    private String examName;
    private String examType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String location;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 无参构造（JavaFX必须要有）
    public Exam() {}

    // 全参构造
    public Exam(Long examId, String examName, String examType, LocalDateTime startTime, LocalDateTime endTime, Integer duration, String location, String status) {
        this.examId = examId;
        this.examName = examName;
        this.examType = examType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.location = location;
        this.status = status;
    }

    // GET/SET 方法（JavaFX表格必须需要）
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}