package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class DiffLevelCorrectRate {
    private int level;
    private int correct_answers;
    private int incorrect_answers;
}