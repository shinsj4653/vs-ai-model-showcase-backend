package visang.showcase.aibackend.dto.request.triton;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeLevelRequest {
    List<KnowledgeObject> inputs;
}
