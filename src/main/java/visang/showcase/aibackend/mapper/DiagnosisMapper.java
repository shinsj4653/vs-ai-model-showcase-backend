package visang.showcase.aibackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultQueryDto;

import java.util.List;

@Mapper
public interface DiagnosisMapper {
    List<DiagnosisProblemDto> getProblems(@Param("memberNo") String memberNo);

    DiagnosisResultQueryDto sendResult(@Param("probSolveIdx") Integer probSolveIdx);
}
