// Parallel batch

package com.com.jeoluspug.batch;

import com.com.jeoluspug.domain.Dept;
import com.com.jeoluspug.domain.Dept2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPageFlowJob {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;


    
    @Bean
    public Job jpaPageFlowJob_batchBuild() {
        // Parallel Batch는 flow를 사용해서 batch를 수행함
        Flow flow1 = new FlowBuilder<Flow>("flow1")
                .start(jpaPageJob_1_batchStep1())
                .build();
        // 기존 step, reader, processor, writer를 병렬 배치할 만큼 세트로 만든 후
        Flow flow2 = new FlowBuilder<Flow>("flow2")
                .start(jpaPageJob_2_batchStep1())
                .build();
        // 아래와 같이 FlowBuilder에 add 하여
        Flow parallelStepFlow = new FlowBuilder<Flow>("parallelStepFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(flow1, flow2)
                .build();
        // parallelStepFlow를 start해서 Parallel Batch, 병렬 배치가 가능함
        return jobBuilderFactory.get("jpaPageFlowJob")
                .start(parallelStepFlow)
                .build().build();

    }

    @Bean
    public Step jpaPageJob_1_batchStep1() {

        return stepBuilderFactory.get("JpaPageFlowJob_1_Step")
                .<Dept, Dept2>chunk(chunkSize)
                .reader(jpaPageJob_1_dbItemReader())
                .processor(jpaPageJob_1_processor())
                .writer(jpaPageJob_1_dbItemWriter())
                .build();
    }

    @Bean
    public Step jpaPageJob_2_batchStep1() {
        return stepBuilderFactory.get("JpaPageFlowJob_2_Step")
                .<Dept, Dept2>chunk(chunkSize)
                .reader(jpaPageJob_2_dbItemReader())
                .processor(jpaPageJob_2_processor())
                .writer(jpaPageJob_2_dbItemWriter())
                .build();
    }



    @Bean
    public JpaPagingItemReader<Dept> jpaPageJob_1_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("JpaPageJob1_Reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                // 5000보다 작거나 같은 데이터를 잘라 batch 처리
                .queryString("SELECT d FROM Dept d where dept_no <= 5000 order by dept_no asc")
                .build();
    }

    @Bean
    public ItemProcessor<Dept, Dept2> jpaPageJob_1_processor() {
        return dept -> new Dept2(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageJob_1_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public JpaPagingItemReader<Dept> jpaPageJob_2_dbItemReader() {
        return new JpaPagingItemReaderBuilder<Dept>()
                .name("JpaPageJob1_Reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                // 5000보다 큰 데이터를 잘라 batch 처리
                .queryString("SELECT d FROM Dept d where dept_no > 5000 order by dept_no asc")
                .build();
    }

    @Bean
    public ItemProcessor<Dept, Dept2> jpaPageJob_2_processor() {
        return dept -> new Dept2(dept.getDeptNo(), "NEW_" + dept.getDName(), "NEW_" + dept.getLoc());
    }

    @Bean
    public JpaItemWriter<Dept2> jpaPageJob_2_dbItemWriter() {
        JpaItemWriter<Dept2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
