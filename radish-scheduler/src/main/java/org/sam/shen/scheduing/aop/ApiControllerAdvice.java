package org.sam.shen.scheduing.aop;

import org.sam.shen.core.model.Resp;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author clock
 * @date 2019/1/10 上午11:05
 */
@ControllerAdvice(basePackages = "org.sam.shen.scheduing.api")
public class ApiControllerAdvice {


    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Resp<String> handleException() {
        return new Resp<>(Resp.FAIL.getCode(), Resp.FAIL.getMsg());
    }

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    public Resp<String> handleRuntimeException(RuntimeException e) {
        return new Resp<>(Resp.FAIL.getCode(), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Resp<String> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return new Resp<>(Resp.FAIL.getCode(), "参数".concat(e.getParameterName()).concat("缺失！"));
    }

}
