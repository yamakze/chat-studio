package com.wokoba.czh.types.framework.tree;

public interface IStrategyMapper<T, D, R> {
    /**
     * 获取下一个待执行策略
     *
     * @param requestParameter 入参
     * @param dynamicContext   上下文
     * @return 返参
     */
    IStrategyHandler<T,D,R> get(T requestParameter, D dynamicContext) throws Exception;
}
