// chunkSize 별 처리 시간
// chunkSize가 10일때 약 16초, 100일때 약 7초, 1000일때 약 6초, 10000일때 약 5초

package com.com.jeoluspug.batch;

import com.com.jeoluspug.domain.Dept;
import com.com.jeoluspug.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분

    @Bean
    public Job jpaPageJob_batchBuild() {
        return jobBuilderFactory.get("jpaPageJob")
                .start(jpaPageJob_batchStep1(0))
                .build();
    }

    @Bean
    @JobScope
    public Step jpaPageJob_batchStep1(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return stepBuilderFactory.get("JpaPageJob1_Step")
                .<Dept, Dept2>chunk(chunkSize)
                .reader(jpaPageJob_dbItemReader(0))
                .processor(jpaPageJob_processor())
                .writer(jpaPageJob_dbItemWriter())
                .build();
    }

    @Bean
    @JobScope
    public JpaPagingItemReader<Dept> jpaPageJob_dbItemReader(@Value("#{jobParameters[chunkSize]}") int chunkSize) {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("JpaPageJob1_Reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept d order by dept_no asc")
                .build();
    }

    @Bean
    public ItemProcessor<Dept, Dept2> jpaPageJob_processor() {
        return dept -> new Dept2(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());


    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageJob_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
