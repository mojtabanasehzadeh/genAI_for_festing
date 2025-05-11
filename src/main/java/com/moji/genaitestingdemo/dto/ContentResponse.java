package com.moji.genaitestingdemo.dto;

import lombok.Data;

@Data
public class ContentResponse {
    private String content;
    private String promptUsed;
}