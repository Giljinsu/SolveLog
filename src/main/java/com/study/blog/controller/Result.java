package com.study.blog.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result<T> {
    private T data;
    private int dataCount;

    public static <T> Result<List<T>> of(List<T> data) {
        return new Result<>(data, data.size());
    }

    public static <T> Result<T> single(T data) {
        return new Result<>(data, 1);
    }

}
