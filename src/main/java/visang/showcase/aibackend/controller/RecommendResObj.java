package visang.showcase.aibackend.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendResObj {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<List<Object>> data;
}