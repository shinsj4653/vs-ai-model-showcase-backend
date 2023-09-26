package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import visang.showcase.aibackend.dto.request.diagnosis.DashboardRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeLevelRequest;
import visang.showcase.aibackend.dto.request.triton.KnowledgeReqObject;
import visang.showcase.aibackend.dto.response.diagnosis.DashboardDto;
import visang.showcase.aibackend.dto.response.diagnosis.DiagnosisProblemDto;
import visang.showcase.aibackend.dto.response.triton.KnowledgeLevelResponse;
import visang.showcase.aibackend.mapper.DiagnosisMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisMapper diagnosisMapper;

    public List<DiagnosisProblemDto> getProblems(String memberNo) {

        // 끝부분 15개의 문항만 클라이언트로 반환
        return diagnosisMapper.getProblems(memberNo).subList(85 , 100);
    }

    public KnowledgeLevelResponse getDashBoardResult(String memberNo, DashboardRequest request) {

        DashboardDto result = new DashboardDto();

        // 트리톤 서버 요청에 필요한 값 -> q_idx, correct, diff_level
        KnowledgeLevelRequest tritonRequest = new KnowledgeLevelRequest();

        List<DiagnosisProblemDto> preList = diagnosisMapper.getProblems(memberNo).subList(0, 85);
        
        // 앞의 85문제 + 학생 진단 후의 15문제 => 총 100 문제
        List<DiagnosisProblemDto> mergedList = Stream.of(preList, request.getProb_list())
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());

//        mergedList.stream()
//                .map(prob -> {
//                    q_idx_list.add(prob.getQ_idx());
//                    correct_list.add(prob.getCorrect());
//                    diff_level_list.add(prob.getDiff_level());
//                    return null;
//                });
        List<Integer> q_idx_list = mergedList.stream()
                .map(m -> m.getQ_idx()).collect(Collectors.toList());  // 토픽 리스트

        List<Integer> correct_list = mergedList.stream()
                .map(m -> m.getCorrect()).collect(Collectors.toList()); // 정오답 리스트

        List<Integer> diff_level_list = mergedList.stream()
                .map(m -> m.getDiff_level()).collect(Collectors.toList()); // 문제 난이도 리스트

        List<KnowledgeReqObject> inputs = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KnowledgeReqObject obj = new KnowledgeReqObject();
            obj.setName("INPUT__" + i);
            obj.setDatatype("INT64");
            List<Integer> shape = new ArrayList<>();
            shape.add(1); // 배치 사이즈
            shape.add(100); // 문항 수
            obj.setShape(shape);

            List<List<Integer>> data = new ArrayList<>();

            if (i == 0)
                data.add(q_idx_list);


            if (i == 1)
                data.add(correct_list);


            if (i == 2)
                data.add(diff_level_list);

            obj.setData(data);
            inputs.add(obj);
        }

        tritonRequest.setInputs(inputs);

        // RestTemplate을 사용하여 트리톤 지식상태 추론 서버의 응답 값 반환
        return postWithKnowledgeLevelTriton(tritonRequest);

//        return result;
    }

    public KnowledgeLevelResponse postWithKnowledgeLevelTriton(KnowledgeLevelRequest request) {

        RestTemplate restTemplate = new RestTemplate();
        KnowledgeLevelResponse responseEntity = restTemplate.postForObject("http://10.214.2.33:8000" + "/v2/models/gkt/infer", request, KnowledgeLevelResponse.class);

        return responseEntity;
    }
}
