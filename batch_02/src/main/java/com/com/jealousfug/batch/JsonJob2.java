// Json 파일을 batch

package com.com.jealouspug.batch;

import com.com.jealouspug.dto.CoinMarket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JsonJob2 {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // chunkSize는 메모리를 위해서 배치를 잘라서 불러오도록 하지만
    // 배치 특성대로 고려를 해봐야함
    // 잘라서 들고오던 중 뒤의 내용이 바뀌거나 할 경우도 있기 때문에
    // 프로젝트별로 잘 고민해봐야 하는 부분
    private static final int chunkSize = 5;

    @Bean
    public Job jsonJob2_batchBuild() {
        // Application 의 Program Arguments에 넣을 job.name
        return jobBuilderFactory.get("jsonJob2")
                .start(jsonJob2_batchStep1())
                .build();
    }

    @Bean
    public Step jsonJob2_batchStep1() {
        return stepBuilderFactory.get("jsonJob2_batchStep1")
                .<CoinMarket, CoinMarket>chunk(chunkSize)
                .reader(jsonJob2_jsonReader())
                .processor(jsonJob2_processor())
                .writer(jsonJob2_jsonWriter())
                .build();
    }

    // 커스텀을 위한 processor
    private ItemProcessor<CoinMarket, CoinMarket> jsonJob2_processor() {

        return coinMarket -> {
            // 읽은 것과 쓰는 것이 1:1 대칭이 안될 수 있음
            // 이럴땐 return을 null을 주면 됨
            if (coinMarket.getMarket().startsWith("KRW")) {
                return new CoinMarket(coinMarket.getMarket(), coinMarket.getKorean_name(), coinMarket.getEnglish_name());
            } else {
                return null;
            }
        };
    }

    @Bean
    public JsonItemReader<CoinMarket> jsonJob2_jsonReader() {
        return new JsonItemReaderBuilder<CoinMarket>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(CoinMarket.class))
                .resource(new ClassPathResource("sample/jsonJob2_input.json"))
                .name("jsonJob2_jsonReader")
                .build();
    }

    @Bean
    public JsonFileItemWriter<CoinMarket> jsonJob2_jsonWriter() {
        return new JsonFileItemWriterBuilder<CoinMarket>()
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource("output/jsonJob2_output.json"))
                .name("jsonJob2_jsonWriter")
                .build();
    }
}
