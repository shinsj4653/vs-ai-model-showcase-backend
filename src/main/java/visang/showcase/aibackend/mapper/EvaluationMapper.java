package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;

import java.util.List;

@Mapper
public interface EvaluationMapper {

    List<EvaluationProblemDto> getProblems(@Param("qIdx") Integer qIdx);

    List<EvaluationProblemDto> getNextRecommendProblem(@Param("qIdx") Integer qIdx);

}