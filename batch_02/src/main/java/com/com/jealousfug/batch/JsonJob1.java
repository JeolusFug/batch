// Json 파일을 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.dto.CoinMarket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJob1 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private static final int chunkSize = 5;

    @Bean
    public Job jsonJob1_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("jsonJob1")
                .start(jsonJob1_batchStep1())
                .build();
    }

    @Bean
    public Step jsonJob1_batchStep1() {
        return stepBuilderFactory.get("jsonJob1_batchStep1")
                .<CoinMarket, CoinMarket>chunk(chunkSize)
                .reader(jsonJob1_jsonReader())
                .writer(coinMarket -> coinMarket.stream().forEach(coinMarket2 -> {
                    log.debug(coinMarket2.toString());
                }))
                .build();
    }

    @Bean
    public JsonItemReader<CoinMarket> jsonJob1_jsonReader() {
        return new JsonItemReaderBuilder<CoinMarket>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarket.class))
                .resource(new ClassPathResource("sample/jsonJob1_input.json"))
                .name("jsonJob1_jsonReader")
                .build();
    }
}
