// 단일 파일을 DB에 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.domain.Dept;
import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob1 {


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;

    @Bean
    public Job csvToJpaJob1_batchBuild() throws Exception {

        return jobBuilderFactory.get("csvToJpaJob1")
                .start(csvToJpaJob1_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob1_batchStep1() throws Exception {
        return stepBuilderFactory.get("csvToJpaJob1_batchStep1")
                .<TwoDto, Dept>chunk(chunkSize)
                .reader(csvToJpaJob1_FileReader())
                .processor(csvToJpaJob1_processor())
                .writer(csvToJpaJob1_dbItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoDto> csvToJpaJob1_FileReader() {
        FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvToJpaJob1_input.csv"));
        // 보통 첫줄은 컬럼명이므로 스킵할 필요가 있음 데이터를 쓸 것이기 때문
        // 이번엔 첫줄이 컬럼명이 아니므로 스킵하지 않음
        // flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoDto> dtoDefaultLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("one", "two");
        // 구분자가 무엇인지. 파일에 맞는 구분자 작성
        delimitedLineTokenizer.setDelimiter(":");

        BeanWrapperFieldSetMapper<TwoDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoDto.class);

        dtoDefaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
        dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        flatFileItemReader.setLineMapper(dtoDefaultLineMapper);

        return flatFileItemReader;
    }


    @Bean
    public ItemProcessor<TwoDto, Dept> csvToJpaJob1_processor() {
        return twoDto -> new Dept(Integer.parseInt(twoDto.getOne()), twoDto.getTwo(), "기타");
    }

    @Bean
    public JpaItemWriter<Dept> csvToJpaJob1_dbItemWriter() {
        JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }

}
