package visang.showcase.aibackend.dto.response.triton;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeResObject {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<Double> data;
}