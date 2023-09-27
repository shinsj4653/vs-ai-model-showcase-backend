package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
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
    public ResponseDto<?> getDashboardResult(HttpSession session, @RequestBody DashboardRequest request) {
        String memberNo = (String) session.getAttribute("memberNo");
        // memberNo 값이 세션에 존재할 경우에만 서비스단 로직 수행
        if (memberNo != null) {
            return ResponseUtil.SUCCESS("진단평가의 대시보드 결과 조회 성공", diagnosisService.getDashBoardResult(memberNo, request));

        } else {
            return ResponseUtil.ERROR("세션에 memberNo가 없습니다.", null);
        }
    }
}