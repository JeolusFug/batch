package com.com.jealouspug.batch;

import com.com.jealouspug.dto.TwoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class MultiJob1 {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 5;


    @Bean
    public Job multiJob1_batchBuild() throws Exception {

        return jobBuilderFactory.get("multiJob1")
                .start(multijob1_batchStep1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step multijob1_batchStep1(@Value("#{jobParameters[version]}")String version) {

        log.debug("-----------");
        log.debug(version);
        log.debug("-----------");

        return stepBuilderFactory.get("multiJob1_batchStep1")
                .<TwoDto, TwoDto>chunk(chunkSize)
                .reader(multiJob1_Reader(null))
                .processor(multiJob1_processor(null))
                .writer(multiJob1_Writer(null))
                .build();
    }


    @Bean
    // @Value("#{jobParameters[inFileName]}" 사용시 Scope 작성
    @StepScope
    public FlatFileItemReader<TwoDto> multiJob1_Reader(@Value("#{jobParameters[inFileName]}")String inFileName) {
        return new FlatFileItemReaderBuilder<TwoDto>()
                .name("multiJob1_Reader")
                // resource 밑의 sample을 의미
                .resource(new ClassPathResource("sample/" + inFileName))
                .delimited().delimiter(":")
                .names("one", "two")
                .targetType(TwoDto.class)
                .recordSeparatorPolicy(new SimpleRecordSeparatorPolicy() {
                    @Override
                    public String postProcess(String record) {
                        // 만약 : 구분자가 아니라면 null 처리하고 읽지 말아라 라는 처리
                        if (record.indexOf(":") == -1) {
                            return null;
                        }
                        // 읽은 대상은 trim 처리를 하여 빈공간을 없앰
                        return record.trim();
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<TwoDto, TwoDto> multiJob1_processor(@Value("#{jobParameters[version]}")String version) {
        log.debug("processor: " + version);
        return twoDto -> new TwoDto(twoDto.getOne(), twoDto.getTwo());
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<TwoDto> multiJob1_Writer(@Value("#{jobParameters[outputFileName]}") String outputFileName) {
        return new FlatFileItemWriterBuilder<TwoDto>()
                .name("multiJob1_writer")
                // resource 밑의 sample이 아닌 batch_02(프로젝트)에 output이라는 폴더
                .resource(new FileSystemResource("output/" + outputFileName))
                .lineAggregator(item -> {
                    return item.getOne() + "--MultiJob1.java--" + item.getTwo();
                })
                .build();
    }

}

