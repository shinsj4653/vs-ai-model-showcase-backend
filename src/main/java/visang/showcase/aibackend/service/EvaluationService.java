package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationDashboardRequest;
import visang.showcase.aibackend.dto.request.evaluation.EvaluationProbRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.EvaluationProblemDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.CheckProbDto;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.EvaluationDashboardResult;
import visang.showcase.aibackend.dto.response.evaluation.dashboard.TopicLevelChangeDto;
import visang.showcase.aibackend.dto.response.study.StudyReadyProbDto;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.mapper.EvaluationMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EvaluationService {
    // 트리톤 서버 URL
    private static final String TRITON_SERVER_URL = "http://106.241.14.35:8000";
    // 추론 기능 URI
    private static final String INFERENCE_URI = "/v2/models/gkt_last/infer";
    // 타겟토픽 지식수준의 기준값
    public static final double EVALUATION_THRESHHOLD = 4.0;

    private final EvaluationMapper evaluationMapper;
    private final DiagnosisMapper diagnosisMapper;

    /**
     * 형성평가 문항 5개 조회
     * @param memberNo 학생번호
     */
    public List<EvaluationProblemDto> getProblems(String memberNo) {
        Integer qIdx = diagnosisMapper.getTgtTopic(memberNo);
        System.out.println(qIdx);
        return evaluationMapper.getProblems(qIdx);
    }

    /**
     * RequestBody의 INPUT__ 요청 객체 생성
     */
    private KnowledgeReqObject createRequestObj(int idx, int probSize, List<Integer> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, probSize); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Integer>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(EvaluationDashboardRequest request, HttpServletRequest httpServletRequest) {
        // 진단평가 문제 100개 + 학습준비 문제 5개 + 진단평가 문제 5개 누적
        HttpSession session = httpServletRequest.getSession();
        var diagnosisResult = (List<DiagnosisProblemDto>) session.getAttribute("diagnosisResult");
        var studyReadyResult = (List<StudyReadyProbDto>) session.getAttribute("studyReadyResult");
        List<EvaluationProbRequest> evaluationResult =  request.getProb_list();

        // 토픽리스트 추출
        List<Integer> diagnosisQIdxs = diagnosisResult.stream()
                .map(m -> m.getQ_idx()).collect(Collectors.toList());
        List<Integer> evaluationQIdxs = evaluationResult.stream()
                .map(m -> m.getQ_idx()).collect(Collectors.toList());

        // 정오답 리스트 추출
        List<Integer> diagnosisCorrects = diagnosisResult.stream()
                .map(m -> m.getCorrect()).collect(Collectors.toList());
        List<Integer> evaluationCorrects = evaluationResult.stream()
                .map(m -> m.getCorrect()).collect(Collectors.toList());

        // 문제 난이도 리스트
        List<Integer> diagnosisDiffs = diagnosisResult.stream()
                .map(m -> m.getDiff_level()).collect(Collectors.toList());
        List<Integer> evaluationDiffs = evaluationResult.stream()
                .map(m -> m.getDiff_level()).collect(Collectors.toList());

        // 먼저 진단평가 + 형성평가 데이터 합치기
        List<Integer> mergedQIdxs = Stream.of(diagnosisQIdxs, evaluationQIdxs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Integer> mergedCorrects = Stream.of(diagnosisCorrects, evaluationCorrects)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<Integer> mergedDiffs = Stream.of(diagnosisDiffs, evaluationDiffs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        
        // 학습준비를 하고 온 상태라면, 학습준비 데이터도 합쳐주기
        if (studyReadyResult != null) {
            List<Integer> studyReadyQIdxs = studyReadyResult.stream()
                    .map(m -> m.getQ_idx()).collect(Collectors.toList());

            List<Integer> studyReadyCorrects = studyReadyResult.stream()
                    .map(m -> m.getCorrect()).collect(Collectors.toList());

            List<Integer> studyReadyDiffs = studyReadyResult.stream()
                    .map(m -> m.getDiff_level()).collect(Collectors.toList());

            mergedQIdxs = Stream.of(mergedQIdxs, studyReadyQIdxs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedCorrects = Stream.of(mergedCorrects, studyReadyCorrects)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            mergedDiffs = Stream.of(mergedDiffs, studyReadyDiffs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, mergedQIdxs.size(), mergedQIdxs));
        inputs.add(createRequestObj(1, mergedCorrects.size(), mergedCorrects));
        inputs.add(createRequestObj(2, mergedDiffs.size(), mergedDiffs));

        return new KnowledgeLevelRequest(inputs);
    }

    /**
     * 트리톤 서버에 POST /v2/models/gkt_last/infer 요청
     *
     * @param request RequestBody 데이터
     * @return KnowledgeLevelResponse 반환
     */
    private KnowledgeLevelResponse postWithKnowledgeLevelTriton(KnowledgeLevelRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject(TRITON_SERVER_URL + INFERENCE_URI, request, KnowledgeLevelResponse.class);

        return responseEntity;
    }

    /**
     * 형성평가 결과 대시보드 데이터 반환
     *
     * @param memberNo 학생번호
     * @param request RequestBody 데이터
     * @param httpServletRequest Session 정보를 가져오기 위한 HttpServletRequest
     * @return EvaluationDashboardResult 반환
     */
    public EvaluationDashboardResult getDashboardResult(String memberNo, EvaluationDashboardRequest request, HttpServletRequest httpServletRequest){
        EvaluationDashboardResult result = new EvaluationDashboardResult();

        // 형성평가 문항들을 누적하여 지식수준 재추론
        KnowledgeLevelRequest tritonRequest = createTritonRequest(request, httpServletRequest);
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);

        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs()
                .get(0)
                .getData();

        // 타겟토픽명
        String topicName = request.getProb_list()
                .get(0)
                .getTopic_nm();

        // 진단평가 직후의 타겟토픽 지식수준
        Double before = (Double) httpServletRequest.getSession()
                .getAttribute("tgtTopicKnowledgeRate");

        // 형성평가 이후의 타겟토픽 지식수준
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double after = knowledgeRates.get(tgtTopic);

        // 타겟 토픽 전후 지식수준
        result.setTopic_level_change(new TopicLevelChangeDto(before, after));

        // 문항인덱스 정보를 추출하기 위한 문항번호 리스트
        List<Integer> prob_nos = request.getProb_list()
                .stream()
                .map((prob) -> prob.getProb_no())
                .collect(Collectors.toList());

        if (after >= EVALUATION_THRESHHOLD) { // 실수로 틀린 문항
            // 틀린문제 문항번호 추출
            List<Integer> prob_idxs = request.getProb_list()
                    .stream()
                    .filter((prob) -> prob.getCorrect() == 0) // 오답인 경우
                    .map((prob) -> prob_nos.indexOf(prob.getProb_no()) + 1)
                    .collect(Collectors.toList());
            // 틀린문제가 존재하는 경우
            if (prob_idxs.size() > 0){
                result.setMistake_prob(new CheckProbDto(prob_idxs, topicName, after));
            } else { // 틀린문제가 없는 경우
                result.setMistake_prob(null);
            }
            // 점검해야 하는 문항정보는 null로 반환
            result.setCheck_prob(null);
        } else { // 점검이 필요한 문항
            // 맞은문제 문항번호 추출
            List<Integer> prob_idxs = request.getProb_list()
                    .stream()
                    .filter((prob) -> prob.getCorrect() == 1) // 정답인 경우
                    .map((prob) -> prob_nos.indexOf(prob.getProb_no()) + 1)
                    .collect(Collectors.toList());
            // 맞은문제가 존재하는 경우
            if (prob_idxs.size() > 0){
                result.setCheck_prob(new CheckProbDto(prob_idxs, topicName, after));
            } else { // 맞은문제가 없는 경우
                result.setCheck_prob(null);
            }
            // 실수로 틀린 문항정보는 null로 반환
            result.setMistake_prob(null);
        }

        return result;
    }
}