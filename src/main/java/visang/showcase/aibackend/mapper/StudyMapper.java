package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.recommend.RecommendProblemDto;

@Mapper
public interface StudyMapper {
    RecommendProblemDto getRecommendProblemWithProbNo(@Param("prob_no") String prob_no);
}