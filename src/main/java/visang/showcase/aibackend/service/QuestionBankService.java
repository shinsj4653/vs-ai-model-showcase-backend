package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.mapper.QuestionBankMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService {

    private final QuestionBankMapper questionBankMapper;

    public String getQuestionExample() {
        return questionBankMapper.getQuestionExample();
    }
}
