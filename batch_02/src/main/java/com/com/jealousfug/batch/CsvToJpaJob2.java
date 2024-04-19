// 다중 파일을 DB에 batch

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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CsvToJpaJob2 {
    // 다중 파일이기 때문에 필요함
    private final ResourceLoader resourceLoader;
    
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private int chunkSize = 10;

    @Bean
    public Job csvToJpaJob2_batchBuild() throws Exception {

        return jobBuilderFactory.get("csvToJpaJob2")
                .start(csvToJpaJob2_batchStep1())
                .build();
    }

    @Bean
    public Step csvToJpaJob2_batchStep1() throws Exception {
        return stepBuilderFactory.get("csvToJpaJob2_batchStep1")
                .<TwoDto, Dept>chunk(chunkSize)
                .reader(csvToJpaJob2_FileReader())
                .processor(csvToJpaJob2_processor())
                .writer(csvToJpaJob2_dbItemWriter())
                // 3.txt를 보면 두번째 라인에는 오류가 난다. 구별자가 :로 틀려져 있기 때문
                // 이때 그냥 돌리면 세번째 라인부터는 실행조차 되지 않는다
                // 이때 이러한 오류들을 스킵할 수 있는 기능
                .faultTolerant()
                // 어떤 오류를 스킵할 것인지(console의 log를 보고 스킵할지 고쳐야 할지 판단)
                .skip(FlatFileParseException.class)
                // 이때 스킵을 최대 몇번까지 허용할 것인지
                // 참고자료 : https://oingdaddy.tistory.com/183
                .skipLimit(100)
                .build();
    }

    @Bean
    public MultiResourceItemReader<TwoDto> csvToJpaJob2_FileReader() {
        MultiResourceItemReader<TwoDto> multiResourceItemReader = new MultiResourceItemReader<>();

        try {
            multiResourceItemReader.setResources(
                    ResourcePatternUtils.getResourcePatternResolver(this.resourceLoader).getResources(
                            "classpath:sample/csvToJpaJob2/*.txt"
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 람다식을 사용해서 간단하게 표현한 Reader 메소드를 사용
        multiResourceItemReader.setDelegate(multiFileItemReader());

        return multiResourceItemReader;
    }

    @Bean
    // CsvToJpaJob1의 Reader 부분을 길게 적었지만 람다식을 사용해서 메소드로 간결하게 표현함
    public FlatFileItemReader<TwoDto> multiFileItemReader() {
        FlatFileItemReader<TwoDto> flatFileItemReader = new FlatFileItemReader<>();

        flatFileItemReader.setLineMapper((line, lineNumber) -> {

            // 파일에 구분자가 무엇인지
            String [] lines = line.split("#");

            return new TwoDto(lines[0], lines[1]);
        });

        return flatFileItemReader;
    }

    @Bean
    public ItemProcessor<TwoDto, Dept> csvToJpaJob2_processor() {
        return twoDto -> new Dept(Integer.parseInt(twoDto.getOne()), twoDto.getTwo(), "기타");
    }

    @Bean
    public JpaItemWriter<Dept> csvToJpaJob2_dbItemWriter() {
        JpaItemWriter<Dept> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);

        return jpaItemWriter;
    }
}
