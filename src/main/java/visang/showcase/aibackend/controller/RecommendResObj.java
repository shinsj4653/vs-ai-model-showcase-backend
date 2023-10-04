package visang.showcase.aibackend.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendResObj implements Serializable {

    private static final long serialVersionUID = 747903773777271338L;

    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<Object> data;
}