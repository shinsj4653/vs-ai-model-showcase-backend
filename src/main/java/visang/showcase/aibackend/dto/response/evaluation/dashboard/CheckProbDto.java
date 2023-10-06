package visang.showcase.aibackend.dto.response.evaluation.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@AllArgsConstructor
public class CheckProbDto {
    private List<Integer> prob_seq_no;
    private String topic_nm;
    private Double topic_level;
//    private Double relation_topic_level;
}