package visang.showcase.aibackend.dto.response.evaluation.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class TopicLevelChangeDto {
    private Double before_level;
    private Double after_level;
}