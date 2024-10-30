package com.sparta.ssaktium.domain.boards.exception;

import com.sparta.ssaktium.domain.common.exception.GlobalException;

import com.sparta.ssaktium.domain.common.exception.GlobalExceptionConst;

public class NotFoundBoardException extends GlobalException {
    public NotFoundBoardException() {
        super(GlobalExceptionConst.NOT_FOUND_BOARD);
    }
}
