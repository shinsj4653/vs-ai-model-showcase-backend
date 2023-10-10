package visang.showcase.aibackend.dto.response.triton;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeLevelResponse {
    String model_name;
    String model_version;
    List<KnowledgeResObject> outputs;
}