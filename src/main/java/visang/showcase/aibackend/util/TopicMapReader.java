package visang.showcase.aibackend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * topicmap_v0.json을 List<List<Integer>>로 변환해주는 클래스
 */
public class TopicMapReader {
    private ObjectMapper objectMapper = new ObjectMapper();
    private ClassPathResource resource = new ClassPathResource("json/topicmap_v0.json");

    public void read() {
        try {
            File file = resource.getFile();

            // json 파일을 List<List<Integer>> 형식으로 변환
            List<List<Integer>> topicMap = objectMapper.readValue(file, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Integer.class)));

            // 결과 출력
            for (List<Integer> row : topicMap) {
                System.out.println(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}