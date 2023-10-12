package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.AreaCategoryDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.TopicInfoRow;

import java.util.List;

@Mapper
public interface DiagnosisMapper {
    List<DiagnosisProblemDto> getProblems(@Param("memberNo") String memberNo);

    List<Integer> getQIdxWithCategory(@Param("categoryCode") String categoryCode);

    Integer getTgtTopic(@Param("memberNo") String memberNo);

    List<TopicInfoRow> getTopicNamesWithQIdxs(@Param("qIdxs") List<Integer> qIdxs);

    String getIntelligenceMapHtml(@Param("memberNo") String memberNo);

    List<AreaCategoryDto> getCategoriesWithMemberNo(@Param("memberNo") String memberNo);

}