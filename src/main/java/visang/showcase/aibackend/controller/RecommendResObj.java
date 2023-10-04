package visang.showcase.aibackend.controller;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendResObj implements Serializable {

    private static final long serialVersionUID = 747903773777271338L;

    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<Object> data;

    public List<List<Object>> getBatchData(int batchSize) {
        int col = data.size() / batchSize;
        List<List<Object>> result = new ArrayList<>();

        for (int i = 0; i < batchSize; i++) {
            List<Object> batch = new ArrayList<>();
            for (int j = 0; j < col; j++) {
                batch.add(data.get(i * col + j));
            }
            result.add(batch);
        }

        return result;
    }
}