package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.dto.request.diagnosis.DiagnosisResultRequest;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisResultQueryDto;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.DiffLevelCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.TopicCorrectRate;
import visang.showcase.aibackend.dto.response.diagnosis.dashboard.WholeCorrectRate;
import visang.showcase.aibackend.mapper.DiagnosisMapper;
import visang.showcase.aibackend.vo.CorrectCounter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {
    private static final String CORRECT_ANSWER_KEY = "yes";
    private static final String WRONG_ANSWER_KEY = "no";

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

    /**
     * 전체 정답률 계산
     *
     * @param resultRequest 진단평가 결과 데이터
     * @return WholeCorrectRate 반환
     */
    public WholeCorrectRate calculateWholeCorrectRate(DiagnosisResultRequest resultRequest) {
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
    public List<DiffLevelCorrectRate> calculateDiffLevelCorrectRates(DiagnosisResultRequest resultRequest) {
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
    public List<TopicCorrectRate> calculateTopicCorrectRates(DiagnosisResultRequest resultRequest) {
        Map<Integer, CorrectCounter> topicRecords = new HashMap<>();
        Map<Integer, String> topicNames = new HashMap<>();

        for (DiagnosisProblemDto prob : resultRequest.getProb_list()) {
            int q_idx = prob.getQ_idx();
            int correct = prob.getCorrect();
            String topic_nm = prob.getTopic_nm();

            // q_idx와 topic_nm 매핑
            topicNames.putIfAbsent(q_idx, topic_nm);
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
}