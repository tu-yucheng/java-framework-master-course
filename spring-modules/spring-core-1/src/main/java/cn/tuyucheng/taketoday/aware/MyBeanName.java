package cn.tuyucheng.taketoday.aware;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanNameAware;

public class MyBeanName implements BeanNameAware {

    @Override
    public void setBeanName(@NotNull String beanName) {
        System.out.println(beanName);
    }
}