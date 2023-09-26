package visang.showcase.aibackend.dto.response.diagnosis.dashboard;

import lombok.Getter;

import java.util.List;

@Getter
public class DifficultyLevelsDto {
    private List<DiffLevelCorrectRate> difficulty_levels;

    public DifficultyLevelsDto(List<DiffLevelCorrectRate> difficulty_levels) {
        this.difficulty_levels = difficulty_levels;
    }
}