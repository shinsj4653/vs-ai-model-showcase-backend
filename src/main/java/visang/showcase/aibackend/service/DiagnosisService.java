package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.*;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.vo.CorrectCounter;
import visang.showcase.aibackend.vo.TopicKnowledge;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {
    // 정답, 오답 카운트에 사용되는 KEY 값
    private static final String CORRECT_ANSWER_KEY = "yes";
    private static final String WRONG_ANSWER_KEY = "no";
    // 강약 판단기준
    public static final double THRESHOLD = 0.6;
    // 토픽 총 개수
    private static final int TOTAL_TOPIC_COUNT = 317;

    // 100개의 문제에 해당하는 category_cd
    private Set<String> categories;
    // category_cd와 category_nm 매핑
    private Map<String, String> categoryNames = new HashMap<>();
    // q_Idx와 topic_nm 매핑
    private Map<Integer, String> topicNames = new HashMap<>();

    private final DiagnosisMapper diagnosisMapper;

    /**
     * 끝부분 15개의 문항만 클라이언트로 반환
     */
    public List<DiagnosisProblemDto> getProblems(String memberNo) {
        return diagnosisMapper.getProblems(memberNo)
                .subList(85, 100);
    }

    /**
     * category_cd를 저장할 Set 생성
     * category_cd와 category_nm 매핑
     * category_cd와 category_nm 매핑
     */
    private void createProbMetaData(List<DiagnosisProblemDto> mergedList) {
        categories = mergedList.stream()
                .map(prob -> {
                    categoryNames.putIfAbsent(prob.getCateg_cd(), prob.getCateg_nm());
                    topicNames.putIfAbsent(prob.getQ_idx(), prob.getTopic_nm());
                    return prob.getCateg_cd();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * RequestBody의 INPUT__ 요청 객체 생성
     */
    private KnowledgeReqObject createRequestObj(int idx, List<Integer> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, 100); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Integer>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(String memberNo, DashboardRequest request, HttpServletRequest httpServletRequest) {

        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);

        // 앞의 85문제 + 학생 진단 후의 15문제 => 총 100 문제
        List<DiagnosisProblemDto> mergedList = Stream.of(preList, request.getProb_list())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
      
        // 문제 100개에 해당하는 category_cd 데이터 저장
        // category, topic 매핑데이터 생성
        createProbMetaData(mergedList);

        List<Integer> q_idx_list = mergedList.stream()
                .map(m -> m.getQ_idx()).collect(Collectors.toList());  // 토픽 리스트

        List<Integer> correct_list = mergedList.stream()
                .map(m -> m.getCorrect()).collect(Collectors.toList()); // 정오답 리스트

        List<Integer> diff_level_list = mergedList.stream()
                .map(m -> m.getDiff_level()).collect(Collectors.toList()); // 문제 난이도 리스트

        // 정오답 리스트 -> 세션에 저장
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("diagnosisResult", correct_list);

        // INPUT__ 객체 생성
        List<KnowledgeReqObject> inputs = new ArrayList<>();
        inputs.add(createRequestObj(0, q_idx_list));
        inputs.add(createRequestObj(1, correct_list));
        inputs.add(createRequestObj(2, diff_level_list));

        return new KnowledgeLevelRequest(inputs);
    }

    /**
     * 트리톤 서버에 POST /v2/models/gkt/infer 요청
     *
     * @param request RequestBody 데이터
     * @return KnowledgeLevelResponse 반환
     */
    private KnowledgeLevelResponse postWithKnowledgeLevelTriton(KnowledgeLevelRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject("http://10.214.2.33:8000" + "/v2/models/gkt/infer", request, KnowledgeLevelResponse.class);

        return responseEntity;
    }

    /**
     * 진단평가 결과 대시보드 데이터 조회
     *
     * @param memberNo 학생 번호
     * @param request  진단평가 결과 데이터
     * @return DashboardDto 반환
     */
    public DashboardDto getDashBoardResult(String memberNo, DashboardRequest request, HttpServletRequest httpServletRequest) {

        // 트리톤 서버에 전송할 RequestBody 생성
        KnowledgeLevelRequest tritonRequest = createTritonRequest(memberNo, request, httpServletRequest);
        // RestTemplate을 사용하여 트리톤 지식상태 추론 서버의 응답 값 반환
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);

        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs()
                .get(0)
                .getData();


        // 멤버의 타깃 토픽에 대한 지식 수준 -> 세션에 저장하기
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double tgtTopicKnowledgeRate = knowledgeRates.get(tgtTopic);

        // 세션을 얻어와서 tgtTopicKnowledgeRate 값을 세션에 저장
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("tgtTopicKnowledgeRate", tgtTopicKnowledgeRate);

        return createDashBoardResponse(request, knowledgeRates);
    }

    /**
     * 결과 대시보드 응답 데이터 생성
     */
    private DashboardDto createDashBoardResponse(DashboardRequest request, List<Double> knowledgeRates) {
        DashboardDto result = new DashboardDto();
        // 전체 정답률 계산
        WholeCorrectRate wholeCorrectRate = calculateWholeCorrectRate(request);
        result.setTotal_questions(wholeCorrectRate.getTotal_questions());
        result.setCorrect_answers(wholeCorrectRate.getCorrect_answers());
        result.setIncorrect_answers(wholeCorrectRate.getIncorrect_answers());
        // 난이도별 정답률 계산
        List<DiffLevelCorrectRate> diffLevelCorrectRates = calculateDiffLevelCorrectRates(request);
        result.setDifficulty_levels(diffLevelCorrectRates);
        // 토픽별 정답률 계산
        List<TopicCorrectRate> topicCorrectRates = calculateTopicCorrectRates(request);
        result.setTopic_answer_result(topicCorrectRates);
        // 영역별 지식수준 계산
        List<AreaKnowledgeResponse> areaKnowledgeResponses = calculateAreaKnowledgeLevel(knowledgeRates);
        result.setSection_level(areaKnowledgeResponses);
        // 강약 지식요인 계산
        StrongWeakKnowledgeResponse strongWeakKnowledgeResponse = calculateKnowledgeStrength(request, knowledgeRates);
        result.setStrong_level(strongWeakKnowledgeResponse.getStrong_level());
        result.setWeak_level(strongWeakKnowledgeResponse.getWeak_level());
        // 앞으로 배울 토픽의 예상 지식 수준
        List<ExpectedTopicResponse> expectedTopics = calculateExpectedKnowledgeLevel(request, knowledgeRates);
        result.setFuture_topic_level_expectation(expectedTopics);
        return result;
    }

    /**
     * 전체 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return WholeCorrectRate 반환
     */
    private WholeCorrectRate calculateWholeCorrectRate(DashboardRequest resultRequest) {
        Map<String, Integer> correctRecords = new HashMap<>();

        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
            int correct = prob.getCorrect();
            if (correct == 0) { // 오답 count
                correctRecords.put(WRONG_ANSWER_KEY, correctRecords.getOrDefault(WRONG_ANSWER_KEY, 0) + 1);
            } else { // 정답 count
                correctRecords.put(CORRECT_ANSWER_KEY, correctRecords.getOrDefault(CORRECT_ANSWER_KEY, 0) + 1);
            }
        }

        int total = resultRequest.getProb_list().size();
        int correct = correctRecords.getOrDefault(CORRECT_ANSWER_KEY, 0);
        int wrong = correctRecords.getOrDefault(WRONG_ANSWER_KEY, 0);

        return new WholeCorrectRate(total, correct, wrong);
    }

    /**
     * 난이도별 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return List<DiffLevelCorrectRate> 반환
     */
    private List<DiffLevelCorrectRate> calculateDiffLevelCorrectRates(DashboardRequest resultRequest) {
        Map<Integer, CorrectCounter> diffLevelRecords = new HashMap<>();

        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
            int diff_level = prob.getDiff_level();
            int correct = prob.getCorrect();

            diffLevelRecords.putIfAbsent(diff_level, new CorrectCounter());

            if (correct == 0) { // 오답 count
                diffLevelRecords.get(diff_level).wrongCountUp();
            } else { // 정답 count
                diffLevelRecords.get(diff_level).correctCountUp();
            }
        }

        return diffLevelRecords.entrySet()
                .stream()
                .map(entry -> {
                    int diff_level = entry.getKey();
                    CorrectCounter counter = entry.getValue();
                    return new DiffLevelCorrectRate(diff_level, counter.getCorrectCount(), counter.getWrongCount());
                })
                .collect(Collectors.toList());
    }

    /**
     * 토픽별 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return List<TopicCorrectRate> 반환
     */
    private List<TopicCorrectRate> calculateTopicCorrectRates(DashboardRequest resultRequest) {
        Map<Integer, CorrectCounter> topicRecords = new HashMap<>();

        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
            int q_idx = prob.getQ_idx();
            int correct = prob.getCorrect();

            topicRecords.putIfAbsent(q_idx, new CorrectCounter());

            if (correct == 0) { // 오답 count
                topicRecords.get(q_idx).wrongCountUp();
            } else { // 정답 count
                topicRecords.get(q_idx).correctCountUp();
            }
        }

        return topicRecords.entrySet()
                .stream()
                .map(entry -> {
                    int q_idx = entry.getKey();
                    CorrectCounter counter = entry.getValue();
                    return new TopicCorrectRate(topicNames.get(q_idx), counter.getCorrectCount(), counter.getWrongCount());
                })
                .collect(Collectors.toList());
    }

    /**
     * 영역별 지식수준 계산
     *
     * @param knowledgeRates 트리톤 서버에서 받은 지식수준 추론 결과
     * @return List<AreaKnowledgeResponse> 반환
     */
    private List<AreaKnowledgeResponse> calculateAreaKnowledgeLevel(List<Double> knowledgeRates) {
        List<AreaKnowledgeResponse> areaKnowledges = new ArrayList<>();
        // 영역 셋을 순환하면서 영역
        for (String categoryCode : categories) {
            List<Integer> qIdxs = diagnosisMapper.getQIdxWithCategory(categoryCode);
            // 영역에 해당하는 토픽들의 지식수준의 합계 계산
            Double sum = qIdxs.stream()
                    .map(qIdx -> knowledgeRates.get(qIdx))
                    .collect(reducing(Double::sum))
                    .get();
            // 지식수준의 평균 계산
            Double avg = sum / qIdxs.size();
            // 소수점 둘째자리까지 반올림
            String knowledgeLevel = String.format("%.2f", avg);

            areaKnowledges.add(new AreaKnowledgeResponse(categoryNames.get(categoryCode), knowledgeLevel, "진단평가"));
        }

        return areaKnowledges;
    }

    /**
     * 강약 지식요인 계산 (각각 3개씩 추출)
     *
     * @return
     */
    private StrongWeakKnowledgeResponse calculateKnowledgeStrength(DashboardRequest request, List<Double> knowledgeRates) {
        // 진단평가에 사용된 토픽들 추출
        Set<Integer> targetTopics = request.getProb_list()
                .stream()
                .map(prob -> prob.getQ_idx())
                .collect(Collectors.toSet());

        // 토픽별 지식수준 추론값 추출
        List<TopicKnowledge> topicKnowledges = new ArrayList<>();
        for (int qIdx : targetTopics) {
            Double knowledgeRate = knowledgeRates.get(qIdx);
            topicKnowledges.add(new TopicKnowledge(qIdx, topicNames.get(qIdx), knowledgeRate));
        }

        // 지식수준을 기준으로 내림차순 정렬
        Collections.sort(topicKnowledges, Comparator.comparing(TopicKnowledge::getKnowledgeRate).reversed());

        // 강한 지식요인 3개 추출
        List<TopicKnowledge> strongKnowledges = topicKnowledges.stream()
                .filter(topicKnowledge -> topicKnowledge.getKnowledgeRate() >= THRESHOLD)
                .limit(3) // 앞에서 3개
                .collect(Collectors.toList());

        // 약한 지식요인 3개 추출
        List<TopicKnowledge> weakKnowledges = topicKnowledges.stream()
                .filter(topicKnowledge -> topicKnowledge.getKnowledgeRate() < THRESHOLD)
                .sorted() // 오름차순 정렬
                .limit(3) // 앞에서 3개
                .collect(Collectors.toList());

        return new StrongWeakKnowledgeResponse(strongKnowledges, weakKnowledges);
    }

    /**
     * 앞으로 배울 토픽의 예상 지식 수준 계산
     *
     * @param request        진단평가 결과 데이터
     * @param knowledgeRates 트리톤 서버에서 받은 지식수준 추론 결과
     * @return List<ExpectedTopicResponse> 반환
     */
    private List<ExpectedTopicResponse> calculateExpectedKnowledgeLevel(DashboardRequest request, List<Double> knowledgeRates) {
        // 진단평가 마지막 문제 토픽 추출
        int lastIdx = request.getProb_list().size() - 1;
        int lastQIdx = request.getProb_list().get(lastIdx).getQ_idx();

        // 5개의 다음 토픽 인덱스 계산
        List<Integer> nextQIdxs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int nextQIdx = (lastQIdx + i) % TOTAL_TOPIC_COUNT; // 0 ~ 316
            nextQIdxs.add(nextQIdx);
        }

        List<ExpectedTopicResponse> result = new ArrayList<>();
        // 토픽 인덱스 5개에 해당하는 토픽이름 데이터 조회 및 지식수준 추출
        diagnosisMapper.getTopicNamesWithQIdxs(nextQIdxs)
                .forEach((row) -> result.add(new ExpectedTopicResponse(row.getTopic_nm(), knowledgeRates.get(row.getQ_idx()))));

        return result;
    }
}