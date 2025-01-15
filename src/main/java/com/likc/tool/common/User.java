package com.likc.tool.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    private Long id;

    private String name;

    private String password;

    private List<String> roleList;

    private List<Long> projectIdList;
}
