package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

@Getter
public class WholeCorrectRate {
    private int total_questions;
    private int correct_answers;
    private int incorrect_answers;
}