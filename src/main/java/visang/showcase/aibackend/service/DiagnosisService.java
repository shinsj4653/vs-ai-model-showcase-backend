package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import visang.showcase.aibackend.dto.request.diagnosis.DiagnosisResultRequest;
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
        return diagnosisMapper.getProblems(memberNo);
    }

    public List<DiagnosisResultDto> sendResult(DiagnosisResultRequest request) {

        List<DiagnosisProblemDto> list = request.getProb_list();

        return list.stream()
                .map(prob -> {
                    DiagnosisResultQueryDto dto = diagnosisMapper.sendResult(prob.getProb_solve_idx());
                    return new DiagnosisResultDto(dto.getProb_solve_idx(), dto.getSubsection_nm(), dto.getTopic_nm(), prob.getCorrect());
                })
                .collect(Collectors.toList());

    }

    public List<String, Object> getDashBoardResult() {


    }
}
