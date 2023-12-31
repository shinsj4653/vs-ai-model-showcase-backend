package visang.showcase.aibackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.diagnosis.DiagnosisDashboardRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.*;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.dto.response.triton.KnowledgeResObject;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.mapper.TransactionMapper;
import visang.showcase.aibackend.vo.CorrectCounter;
import visang.showcase.aibackend.vo.TopicKnowledge;

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
    private static final double THRESHOLD = 4.0;
    // 토픽 총 개수
    private static final int TOTAL_TOPIC_COUNT = 317;
    // 트리톤 서버 URL
    private static final String TRITON_SERVER_URL = "http://106.241.14.35:8000";
    // 추론 기능 URI
    private static final String INFERENCE_URI = "/v2/models/gkt_last/infer";

    // 100개의 문제에 해당하는 category_cd
    private Set<String> categories;

    // 타깃 토픽에 해당하는 영역들의 category_cd
    private Set<String> categoriesWithArea;


    // category_cd와 category_nm 매핑
    private Map<String, String> categoryNames = new HashMap<>();
    // q_Idx와 topic_nm 매핑
    private Map<Integer, String> topicNames = new HashMap<>();

    private final DiagnosisMapper diagnosisMapper;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;

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
    private KnowledgeReqObject createRequestObj(int idx, List<Long> payload) {
        KnowledgeReqObject obj = new KnowledgeReqObject();
        obj.setName("INPUT__" + idx);
        obj.setDatatype("INT64");

        List<Integer> shape = List.of(1, 100); // 배치 사이즈, 문항 수
        obj.setShape(shape);

        List<List<Long>> data = List.of(payload);
        obj.setData(data);

        return obj;
    }

    /**
     * 트리톤 서버에 전송할 RequestBody 생성
     */
    private KnowledgeLevelRequest createTritonRequest(String memberNo, DiagnosisDashboardRequest request, String token) {

        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);

        // 앞의 85문제 + 학생 진단 후의 15문제 => 총 100 문제
        List<DiagnosisProblemDto> mergedList = Stream.of(preList, request.getProb_list())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // 문제 100개에 해당하는 category_cd 데이터 저장
        // category, topic 매핑데이터 생성
        createProbMetaData(mergedList);

        // 문제 100개에 해당하는 리스트 -> DB에 저장
        try {
            String diagnosis_data = objectMapper.writeValueAsString(mergedList);
            transactionMapper.updateDiagnosisData(token, diagnosis_data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<Long> q_idx_list = mergedList.stream()
                .map(m -> Long.valueOf(m.getQ_idx())).collect(Collectors.toList());  // 토픽 리스트

        List<Long> correct_list = mergedList.stream()
                .map(m -> Long.valueOf(m.getCorrect())).collect(Collectors.toList()); // 정오답 리스트

        List<Long> diff_level_list = mergedList.stream()
                .map(m -> Long.valueOf(m.getDiff_level())).collect(Collectors.toList()); // 문제 난이도 리스트
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
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject(TRITON_SERVER_URL + INFERENCE_URI, request, KnowledgeLevelResponse.class);
        return responseEntity;
    }

    /**
     * 진단평가 결과 대시보드 데이터 조회
     *
     * @param memberNo 학생 번호
     * @param request  진단평가 결과 데이터
     * @return DashboardDto 반환
     */
    public DashboardDto getDashBoardResult(String memberNo, DiagnosisDashboardRequest request, String token) {

        // 트리톤 서버에 전송할 RequestBody 생성
        KnowledgeLevelRequest tritonRequest = createTritonRequest(memberNo, request, token);
        // RestTemplate을 사용하여 트리톤 지식상태 추론 서버의 응답 값 반환
        KnowledgeLevelResponse response = postWithKnowledgeLevelTriton(tritonRequest);

        // 지식수준 추론 결과
        List<Double> knowledgeRates = response.getOutputs()
                .get(0)
                .getData();

        // 멤버의 타깃 토픽에 대한 지식 수준 -> DB에 저장하기
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double tgtTopicKnowledgeRate = knowledgeRates.get(tgtTopic);
        transactionMapper.updateTgtTopicKnowledgeRate(token, tgtTopicKnowledgeRate);

        return createDashBoardResponse(memberNo, request, knowledgeRates);
    }

    /**
     * 결과 대시보드 응답 데이터 생성
     */
    private DashboardDto createDashBoardResponse(String memberNo, DiagnosisDashboardRequest request, List<Double> knowledgeRates) {
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
        List<AreaKnowledgeResponse> areaKnowledgeResponses = calculateAreaKnowledgeLevel(memberNo, knowledgeRates);
        result.setSection_level(areaKnowledgeResponses);
        // 강약 지식요인 계산
        StrongWeakKnowledgeResponse strongWeakKnowledgeResponse = calculateKnowledgeStrength(request, knowledgeRates);
        result.setStrong_level(strongWeakKnowledgeResponse.getStrong_level());
        result.setWeak_level(strongWeakKnowledgeResponse.getWeak_level());

        // 앞으로 배울 토픽의 예상 지식 수준
        List<ExpectedTopicResponse> expectedTopics = calculateExpectedKnowledgeLevel(memberNo, request, knowledgeRates);
        result.setFuture_topic_level_expectation(expectedTopics);

        // 지식 맵 html 코드
        String intelligenceMapHtml = diagnosisMapper.getIntelligenceMapHtml(memberNo);
        result.setIntelligence_map_html(intelligenceMapHtml);

        return result;
    }

    /**
     * 전체 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return WholeCorrectRate 반환
     */
    private WholeCorrectRate calculateWholeCorrectRate(DiagnosisDashboardRequest resultRequest) {
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
    private List<DiffLevelCorrectRate> calculateDiffLevelCorrectRates(DiagnosisDashboardRequest resultRequest) {
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
    private List<TopicCorrectRate> calculateTopicCorrectRates(DiagnosisDashboardRequest resultRequest) {
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
    private List<AreaKnowledgeResponse> calculateAreaKnowledgeLevel(String memberNo, List<Double> knowledgeRates) {
        List<AreaKnowledgeResponse> areaKnowledges = new ArrayList<>();

        // 학생에 해당하는 영역 카테고리 정보
        List<AreaCategoryDto> categoriesWithMemberNo = diagnosisMapper.getCategoriesWithMemberNo(memberNo);
        Map<String, List<AreaCategoryDto>> categoryMap = categoriesWithMemberNo.stream()
                .collect(Collectors.groupingBy(AreaCategoryDto::getCateg_nm));


        for (String s : categoryMap.keySet()) {
            List<AreaCategoryDto> areaCategoryDtos = categoryMap.get(s);
            Double sum = areaCategoryDtos.stream()
                    .map(dto -> knowledgeRates.get(dto.getQ_idx()))
                    .collect(reducing(Double::sum))
                    .get();

           // 지식수준의 평균 계산
             Double avg = sum / areaCategoryDtos.size();
            // 소수점 셋째자리까지 반올림
            Double knowledgeLevel = Double.valueOf(String.format("%.2f", avg));

            areaKnowledges.add(new AreaKnowledgeResponse(s, knowledgeLevel, "진단평가"));

        }

//        for (int i = 0; i < categoryMap.size() - 1; i++) {
//            List<AreaCategoryDto> areaCategoryDtos = categoryMap.get(i);
//            Double sum = areaCategoryDtos.stream()
//                    .map(dto -> knowledgeRates.get(dto.getQIdx()))
//                    .collect(reducing(Double::sum))
//                    .get();
//
//            for (AreaCategoryDto areaCategoryDto : areaCategoryDtos) {
//                sum += knowledgeRates.get(areaCategoryDto.getQIdx());
//            }
//
//            // 지식수준의 평균 계산
//            Double avg = sum / areaCategoryDtos.size();
//            // 소수점 셋째자리까지 반올림
//            Double knowledgeLevel = Double.valueOf(String.format("%.2f", avg));
//
//            areaKnowledges.add(new AreaKnowledgeResponse(categoryMap., knowledgeLevel, "진단평가"));
//        }

        // 영역 셋을 순환하면서 영역
//        for (AreaCategoryDto categ : categoriesWithMemberNo) {
//            List<Integer> qIdxs = diagnosisMapper.getQIdxWithCategory();
//            // 영역에 해당하는 토픽들의 지식수준의 합계 계산
//            Double sum = qIdxs.stream()
//                    .map(qIdx -> knowledgeRates.get(qIdx))
//                    .collect(reducing(Double::sum))
//                    .get();
//            // 지식수준의 평균 계산
//            Double avg = sum / qIdxs.size();
//            // 소수점 셋째자리까지 반올림
//            Double knowledgeLevel = Double.valueOf(String.format("%.2f", avg));
//
//            areaKnowledges.add(new AreaKnowledgeResponse(categoryNames.get(categoryCode), knowledgeLevel, "진단평가"));
//        }

        return areaKnowledges;
    }

    /**
     * 강약 지식요인 계산 (각각 3개씩 추출)
     *
     * @return
     */
    private StrongWeakKnowledgeResponse calculateKnowledgeStrength(DiagnosisDashboardRequest request, List<Double> knowledgeRates) {
        // 진단평가에 사용된 토픽들 추출
        Set<Integer> targetTopics = request.getProb_list()
                .stream()
                .map(prob -> prob.getQ_idx())
                .collect(Collectors.toSet());

        // 토픽별 지식수준 추론값 추출
        List<TopicKnowledge> topicKnowledges = new ArrayList<>();
        for (int qIdx : targetTopics) {
            Double knowledgeRate = Double.valueOf(String.format("%.2f", knowledgeRates.get(qIdx)));
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
    private List<ExpectedTopicResponse> calculateExpectedKnowledgeLevel(String memberNo, DiagnosisDashboardRequest request, List<Double> knowledgeRates) {
        // 진단평가 마지막 문제 토픽 추출
//        int lastIdx = request.getProb_list().size() - 1;
//        int lastQIdx = request.getProb_list().get(lastIdx).getQ_idx();
        
        // 진단평가의 마지막문항의 토픽 idx가 기준이 아닌, 학생의 타켓 토픽 idx가 기준
        Integer tgtTopicIdx = diagnosisMapper.getTgtTopic(memberNo);

        // 5개의 다음 토픽 인덱스 계산
        List<Integer> nextQIdxs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int nextQIdx = (tgtTopicIdx + i) % TOTAL_TOPIC_COUNT; // 0 ~ 316
            nextQIdxs.add(nextQIdx);
        }

        List<ExpectedTopicResponse> result = new ArrayList<>();
        // 토픽 인덱스 5개에 해당하는 토픽이름 데이터 조회 및 지식수준 추출
//        diagnosisMapper.getTopicNamesWithQIdxs(nextQIdxs)
//                .forEach((row) -> result.add(new ExpectedTopicResponse(row.getTopic_nm(), Double.valueOf(String.format("%.2f", knowledgeRates.get(row.getQ_idx()))))));

        // 앞으로 배울 토픽이름 및 지식수준
        // => 타겟 토픽 지식 수준, 타겟 토픽 명으로 세팅
        Integer tgtTopic = diagnosisMapper.getTgtTopic(memberNo);
        Double tgtTopicKnowledgeRate = Double.valueOf(String.format("%.2f", knowledgeRates.get(tgtTopic)));
        String tgtTopicName = diagnosisMapper.getTgtTopicName(tgtTopic);

        result.add(new ExpectedTopicResponse(tgtTopicName, tgtTopicKnowledgeRate));
        return result;
    }
}