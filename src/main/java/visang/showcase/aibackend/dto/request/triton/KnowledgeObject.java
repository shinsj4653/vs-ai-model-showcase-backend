package visang.showcase.aibackend.dto.request.triton;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KnowledgeObject {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<List<Integer>> data;
}
