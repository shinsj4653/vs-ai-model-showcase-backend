package visang.showcase.aibackend.dto.request.triton;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeLevelRequest {
    private final List<KnowledgeReqObject> inputs;
}