package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultQueryDto;
import visang.showcase.aibackend.mapper.DiagnosisMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisMapper diagnosisMapper;

    public List<DiagnosisProblemDto> getProblems(String memberNo) {

        // 끝부분 15개의 문항만 클라이언트로 반환
        return diagnosisMapper.getProblems(memberNo).subList(85 , 100);
    }

//    public List<String, Object> getDashBoardResult() {
//
//    }
}
