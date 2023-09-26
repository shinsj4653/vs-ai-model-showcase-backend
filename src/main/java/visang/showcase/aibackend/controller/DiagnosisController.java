package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
<<<<<<< HEAD
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
=======
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiagnosisDashboardResultDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiffLevelCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.TopicCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.WholeCorrectRate;
>>>>>>> api-diagnosis-dashboard
import visang.showcase.aibackend.service.DiagnosisService;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @GetMapping("getProblems")
    public ResponseDto<List<DiagnosisProblemDto>> getProblems(HttpSession session) {
        String memberNo = (String) session.getAttribute("memberNo");
        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            List<DiagnosisProblemDto> problems = diagnosisService.getProblems(memberNo);
            return ResponseUtil.SUCCESS("타켓 학생의 문제 풀이 시퀀스, 응답 시퀀스 조회 성공 ", problems);
        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }

    @PostMapping("dashboard")
    public ResponseDto<KnowledgeLevelResponse> getDashboardResult(HttpSession session, @RequestBody DashboardRequest request) {
        String memberNo = (String) session.getAttribute("memberNo");
        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가의 대시보드 결과 조회 성공", diagnosisService.getDashBoardResult(memberNo, request));

        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }

<<<<<<< HEAD
}
=======
    /**
     * 트리톤 서버에서 받은 데이터와 조합하여 응답해야 함
     * Map<String, Object> 형식으로 반환 예정
     * 테스트를 위해 임시로 DiagnosisDashboardResultDto를 사용
     */
    @PostMapping("dashboard")
    public ResponseDto<DiagnosisDashboardResultDto> getDiagnosisDashboardResult(@RequestBody DiagnosisResultRequest resultRequest) {

        WholeCorrectRate wholeCorrectRate = diagnosisService.calculateWholeCorrectRate(resultRequest);
        List<DiffLevelCorrectRate> diffLevelCorrectRates = diagnosisService.calculateDiffLevelCorrectRates(resultRequest);
        List<TopicCorrectRate> topicCorrectRates = diagnosisService.calculateTopicCorrectRates(resultRequest);

        DiagnosisDashboardResultDto result = new DiagnosisDashboardResultDto(wholeCorrectRate, diffLevelCorrectRates, topicCorrectRates);

        return ResponseUtil.SUCCESS("진단평가 대시보드 데이터 조회 성공", result);
    }
}
>>>>>>> api-diagnosis-dashboard
