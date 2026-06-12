package com.teach.javafx.request;

/**
 * JwtResponse JWT数据返回对象 包含客户登录的信息
 * String tokenType token字符串
 * Integer id 用户的ID user_id
 * String username 用户的登录名
 * String token 登录客户加密数据串 请求是要传到后端进行权限验证
 * String role 用户角色 ROLE_ADMIN, ROLE_STUDENT, ROLE_TEACHER
 * String perName 用户姓名
 */

public class JwtResponse {
    private String tokenType;
    private Integer id;
    private String username;
    private String token;
    private String role;
    private String perName;

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPerName() {
        return perName;
    }

    public void setPerName(String perName) {
        this.perName = perName;
    }
}