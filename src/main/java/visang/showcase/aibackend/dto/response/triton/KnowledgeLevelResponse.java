package visang.showcase.aibackend.dto.response.triton;

import lombok.Data;
import visang.showcase.aibackend.dto.request.triton.KnowledgeObject;

import java.util.List;

@Data
public class KnowledgeLevelResponse {
    List<KnowledgeObject> outputs;
}
