// fixed 파일을 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FixedLengthJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 5;

    @Bean
    public Job fixedLengthJob1_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("fixedLengthJob1")
                .start(fixedLengthJob1_batchStep1())
                .build();
    }

    @Bean
    public Step fixedLengthJob1_batchStep1() {
        return stepBuilderFactory.get("fixedLengthJob1_batchStep1")
                .<TwoDto, TwoDto>chunk(chunkSize)
                .reader(fixedLengthJob1_FileReader())
                .writer(twoDto -> twoDto.stream().forEach(twoDto2 -> {
                    log.debug(twoDto2.toString());
                }))
                .build();
    }
    @Bean
    public FlatFileItemReader<TwoDto> fixedLengthJob1_FileReader() {
        FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("/sample/fixedLengthJob1_input.txt"));
        flatFileItemReader.setLinesToSkip(1);

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
}
