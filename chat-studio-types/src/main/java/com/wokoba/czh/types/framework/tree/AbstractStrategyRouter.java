package com.wokoba.czh.types.framework.tree;

import lombok.Getter;
import lombok.Setter;

public abstract class AbstractStrategyRouter<T, D, R> implements IStrategyHandler<T, D, R>, IStrategyMapper<T, D, R> {
    @Getter
    @Setter
    protected IStrategyHandler<T, D, R> defaultStrategyHandler = IStrategyHandler.DEFAULT;

    public R router(T requestParameter, D dynamicContext) throws Exception{
        IStrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        return strategyHandler != null ? apply(requestParameter, dynamicContext) : defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }
}
