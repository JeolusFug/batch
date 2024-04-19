// 엑셀과 같은 csv 파일을 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
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
public class CsvJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;

    @Bean
    public Job csvJob1_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("csvJob1")
                .start(csvJob1_batchStep1())
                .build();
    }

    @Bean
    public Step csvJob1_batchStep1() {
        return stepBuilderFactory.get("csvJob1_batchStep1")
                .<TwoDto, TwoDto>chunk(chunkSize)
                .reader(csvJob1_FileReader())
                .writer(twoDto -> twoDto.stream().forEach(twoDto2 -> {
                        log.debug(twoDto2.toString());
                }))
                .build();
    }

    @Bean
    public FlatFileItemReader<TwoDto> csvJob1_FileReader() {
        FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/csvJob1_input.csv"));

        // 보통 첫줄은 컬럼명이므로 스킵할 필요가 있음 데이터를 쓸 것이기 때문
        flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoDto> dtoDefaultLineMapper  = new DefaultLineMapper<>();

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
}
