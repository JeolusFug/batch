// DB를 읽어 Batch

package com.com.jealouspug.batch;

import com.com.jealouspug.domain.Dept;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob1 {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;

    @Bean
    public Job JpaPageJob1_batchBuild() {       // Job뒤의 이름은 다른 곳에서도 중복되면 오류 발생
                                                // Bean은 유일해야함

        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("JpaPageJob1")
                .start(JpaPageJob1_step1()).build();
    }

    @Bean
    public Step JpaPageJob1_step1() {
        return stepBuilderFactory.get("JpaPageJob1_step1")
                .<Dept, Dept>chunk(chunkSize)
                .reader(jpaPageJob1_dbItemReader())
                .writer(jpaPageJob1_printItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJob1_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("jpaPageJob1_dbItemReaderZ")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept d order by dept_no asc")
                .build();
    }

    @Bean
    public ItemWriter<Dept> jpaPageJob1_printItemWriter() {
        return list -> {
            for (Dept dept : list) {
                log.debug(dept.toString());
            }
        };
    }
}
