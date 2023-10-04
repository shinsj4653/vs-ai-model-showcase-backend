package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.study.RecommendProblemDto;

import java.util.List;

@Mapper
public interface StudyMapper {
    List<RecommendProblemDto> getRecommendProblemWithQIdx(@Param("qIdx") Integer qIdx);
}