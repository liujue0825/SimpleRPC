package com.lj.rpc.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liujue
 * @version 1.0
 * @date 2024/3/1 12:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    private String username;

    private String password;

    private Integer age;
}

