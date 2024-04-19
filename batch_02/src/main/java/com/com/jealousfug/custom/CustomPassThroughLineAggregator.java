package com.com.jealouspug.custom;

import com.com.jealouspug.dto.OneDto;
import org.springframework.batch.item.file.transform.LineAggregator;

// 이런 형식으로 커스텀 하는것은 좋지 않음
// 가능하다는 것만 알아야함
// 커스텀음 processor을 통해서 할 것
public class CustomPassThroughLineAggregator<T> implements LineAggregator<T> {
    @Override
    public String aggregate(T item) {

        if (item instanceof OneDto) {
            return item.toString() + "_item";
        }

        return item.toString();
    }
}
