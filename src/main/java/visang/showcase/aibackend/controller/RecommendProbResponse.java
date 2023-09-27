package visang.showcase.aibackend.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendProbResponse {
    private List<RecommendResObj> outputs;
}