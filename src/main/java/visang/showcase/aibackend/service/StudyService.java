package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.dto.response.study.StudyReadyDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {
    public StudyReadyDto isStudyReady(Double tgtTopicKnowledgeRate){
        // 타켓토픽의 지식 수준이 특정 기준을 못 넘길시, 학습할 준비가 안되었다고 판단
        if (tgtTopicKnowledgeRate >= 0.5)
            return new StudyReadyDto(true);
        else
            return new StudyReadyDto(false);
    }
}
