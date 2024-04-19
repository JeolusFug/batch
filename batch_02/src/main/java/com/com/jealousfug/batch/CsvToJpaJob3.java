// 파일을 DB에 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.domain.Two;
import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob3 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 5;


    @Bean
    public Job csvToJpaJob3_batchBuild() throws Exception {
        return jobBuilderFactory.get("csvToJpaJob3")
                .start(csvToJpaJob3_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob3_batchStep1() throws Exception {
        return stepBuilderFactory.get("csvToJpaJob3_batchStep1")
                .<TwoDto, Two>chunk(chunkSize)
                .reader(csvToJpaJob3_Reader(null))
                .processor(csvToJpaJob3_processor())
                .writer(csvToJpaJob3_dbItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TwoDto> csvToJpaJob3_Reader(@Value("#{jobParameters[inFileName]}")String inFileName) {
        return new FlatFileItemReaderBuilder<TwoDto>()
                .name("multiJob1_Reader")
                .resource(new FileSystemResource(inFileName))
                .delimited().delimiter(":")
                .names("one","two")
                .targetType(TwoDto.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy(){
                    @Override
                    public String postProcess(String record) {
                        log.debug("policy: "+record);
                        // 파서 대상이 아니면 무시해줌.
                        if(record.indexOf(":") == -1){
                            return null;
                        }
                        return record.trim();
                    }
                }).build();
    }

    @Bean
    public ItemProcessor<TwoDto, Two> csvToJpaJob3_processor() {
        return twoDto -> new Two(twoDto.getOne(), twoDto.getTwo());
    }

    @Bean
    public JpaItemWriter<Two> csvToJpaJob3_dbItemWriter() {
        JpaItemWriter<Two> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
