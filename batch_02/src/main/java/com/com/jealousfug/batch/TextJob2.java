// text 파일을 읽어 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.custom.CustomPassThroughLineAggregator;
import com.com.jealouspug.dto.OneDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TextJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 5;

    @Bean
    public Job textJob2_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("textJob2")
                .start(textJob2_batchStep1())
                .build();
    }

    @Bean
    public Step textJob2_batchStep1() {
        return stepBuilderFactory.get("textJob2_batchStep1")
                .<OneDto, OneDto>chunk(chunkSize)
                .reader(textJob2_FileReader())
                .writer(textJob2_FileWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<OneDto> textJob2_FileReader() {
        FlatFileItemReader<OneDto> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("sample/textJob2_input.txt"));
        flatFileItemReader.setLineMapper(((line, lineNumber) -> new OneDto(lineNumber + "==" + line)));

        return flatFileItemReader;
    }

    @Bean
    public FlatFileItemWriter textJob2_FileWriter() {
        return new FlatFileItemWriterBuilder<OneDto>()
                .name("textJob2_FileWriter")
                .resource(new FileSystemResource("output/textJob2_output.txt"))
                // new CustomPassThroughLineAggregator 과 같이 
                // Reader 혹은 Writer 단에서 커스텀 하는것은 좋지 않음
                // 가능하다는 것만 알아야함
                // 커스텀음 processor을 통해서 할 것
                // JsonJob2에 processor 구현 참고
                .lineAggregator(new CustomPassThroughLineAggregator<>())
                .build();
    }
}
