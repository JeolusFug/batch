package com.com.jealouspug.custom;

import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomBeanWrapperFieldExtractor <T> implements FieldExtractor<T>, InitializingBean {
    private String[] names;

    public void setNames(String[] names) {
        Assert.notNull(names, "Names must be non-null");
        this.names = (String[]) Arrays.asList(names).toArray(new String[names.length]);
    }

    public Object[] extract(T item) {
        List<Object> values = new ArrayList();
        BeanWrapper bw = new BeanWrapperImpl(item);
        String[] var4 = this.names;
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String propertyName = var4[var6];
            values.add(bw.getPropertyValue(propertyName));
        }

        return values.toArray();
    }

    public void afterPropertiesSet() {
        Assert.notNull(this.names, "The 'names' property must be set.");
    }
}
