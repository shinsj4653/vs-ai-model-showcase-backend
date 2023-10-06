package visang.showcase.aibackend.dto.response.triton;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendProbResponse implements Serializable {

    private static final long serialVersionUID = -1192156855321141210L;

    private String model_name;
    private String model_version;
    private List<RecommendResObj> outputs;

}