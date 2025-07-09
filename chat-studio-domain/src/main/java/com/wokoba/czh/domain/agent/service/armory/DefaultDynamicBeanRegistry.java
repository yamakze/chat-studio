package com.wokoba.czh.domain.agent.service.armory;

import com.wokoba.czh.domain.agent.service.CustomBeanRegistrar;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultDynamicBeanRegistry implements CustomBeanRegistrar {
    private final ApplicationContext applicationContext;

    public DefaultDynamicBeanRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @SneakyThrows
    @Override
    public void clearBean(String beanName) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        if (beanFactory.containsBean(beanName)) {
            Object existing = beanFactory.getBean(beanName);
            if (existing instanceof AutoCloseable closeable) {
                closeable.close();
            }

            // 移除旧的定义
            beanFactory.destroySingleton(beanName);
            beanFactory.removeBeanDefinition(beanName);
        }
    }

    @Override
    public <T> T getBean(String beanName) {
        if (applicationContext.containsBean(beanName)) {
            return (T) applicationContext.getBean(beanName);
        }
        return null;
    }

    @Override
    public synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        if (beanFactory.containsBean(beanName)) return;
        // 注册Bean
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanFactory.registerBeanDefinition(beanName, beanDefinition);

        log.info("成功注册Bean: {}", beanName);
    }

}
