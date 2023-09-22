package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.mapper.HomeMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeMapper homeMapper;

    public String getQuestionExample() {
        return homeMapper.getQuestionExample();
    }
}
