package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "to_do_list")
public class TodoList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer personId;
    private String content;
    private String time;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPersonId() { return personId; }
    public void setPersonId(Integer personId) { this.personId = personId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}