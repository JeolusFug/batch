// DB를 읽어 Batch

package com.com.jealouspug.batch;

import com.com.jealouspug.domain.Dept;
import com.com.jealouspug.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageJob2 {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;

    @Bean
    public Job JpaPageJob2_batchBuild() {       // Job뒤의 이름은 다른 곳에서도 중복되면 오류 발생
                                                // Bean은 유일해야함

        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("JpaPageJob2")
                .start(JpaPageJob2_step1()).build();
    }

    @Bean
    public Step JpaPageJob2_step1() {
        return stepBuilderFactory.get("JpaPageJob2_step1")
                .<Dept, Dept2>chunk(chunkSize)
                .reader(JpaPageJob2_dbItemReader())
                .processor(JpaPageJob2_processor())
                .writer(JpaPageJob2_dbItemWriter())
                .build();
    }

    private ItemProcessor<Dept, Dept2> JpaPageJob2_processor() {
        return dept -> {
            return new Dept2(dept.getDeptNo(),"New_" + dept.getDName(), "New_" + dept.getLoc());
        };
    }

    @Bean
    public JpaPagingItemReader<Dept> JpaPageJob2_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("JpaPageJob2_dbItemReaderZ")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM Dept d order by dept_no asc")
                .build();
    }

    @Bean
    public JpaItemWriter<Dept2> JpaPageJob2_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
