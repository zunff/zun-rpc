package com.zjh.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用户实体类
 *
 * @author zunf
 * @date 2024/5/6 15:54
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    /**
     * 用户名
     */
    private String name;

    /**
     * 性别
     */
    private Integer age;

    private static final long serialVersionUID = 1L;
}
