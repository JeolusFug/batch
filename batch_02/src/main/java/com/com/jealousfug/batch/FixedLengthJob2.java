// fixed 파일을 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FixedLengthJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 5;

    @Bean
    public Job fixedLengthJob2_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("fixedLengthJob2")
                .start(fixedLengthJob2_batchStep1())
                .build();
    }

    private Step fixedLengthJob2_batchStep1() {
        return stepBuilderFactory.get("fixedLengthJob2_batchStep1")
                .<TwoDto, TwoDto>chunk(chunkSize)
                .reader(fixedLengthJob2_FileReader())
                // 여기 new 에서 fixedLengthJob2_FileWriter 의 resource 위치 설정
                .writer(fixedLengthJob2_FileWriter(new FileSystemResource("output/fixedLengthJob2_output.txt")))
                .build();
    }

    private FlatFileItemReader<TwoDto> fixedLengthJob2_FileReader() {
        FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("/sample/fixedLengthJob2_input.txt"));
        // flatFileItemReader.setLinesToSkip(1);

        DefaultLineMapper<TwoDto> dtoDefaultLineMapper = new DefaultLineMapper<>();

        FixedLengthTokenizer fixedLengthTokenizer = new FixedLengthTokenizer();

        // DTO와 동일하게 작성하는 듯 함
        fixedLengthTokenizer.setNames("one", "two");
        // 몇개씩 잘라서 할지
        fixedLengthTokenizer.setColumns(new Range(1, 5), new Range(6, 10));

        BeanWrapperFieldSetMapper<TwoDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TwoDto.class);

        dtoDefaultLineMapper.setLineTokenizer(fixedLengthTokenizer);
        dtoDefaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(dtoDefaultLineMapper);

        return flatFileItemReader;
    }

    // throws Exception 추가해 줘야 할 수도 있음
    // 예외가 발생할 수도 있기 때문에
    // 오류 발생 안해서 생략함
    @Bean
    public FlatFileItemWriter<TwoDto> fixedLengthJob2_FileWriter(Resource resource) {
        BeanWrapperFieldExtractor<TwoDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"one", "two"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<TwoDto> lineAggregator = new FormatterLineAggregator<>();

        // 왼쪽, 오른쪽
        // C의 포멧 5s는 오른쪽 정렬 -5s는 왼쪽 정렬
        lineAggregator.setFormat("%-5s!!@#!$!!%5s");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<TwoDto>()
                .name("fixedLengthJob2_FileWriter")
                .resource(resource)
                .lineAggregator(lineAggregator)
                .build();
    }
}
