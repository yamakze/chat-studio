package com.wokoba.czh.domain.agent.service;

public interface CustomBeanRegistrar {

    /**
     * 根据 Bean 名称获取实例
     */
    <T> T getBean(String beanName);

    /**
     * 注册（或替换）一个 Bean 到容器中
     */
    <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance);

    /**
     * 销毁指定的 Bean 单例（适用于重新注册）
     */
    void clearBean(String beanName);
}
