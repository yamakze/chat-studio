package com.wokoba.czh.types.framework.tree;

public interface IStrategyHandler<T, D, R> {
    IStrategyHandler DEFAULT =(T, D) -> null;

    /**
     * 执行策略
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 返参
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;
}
