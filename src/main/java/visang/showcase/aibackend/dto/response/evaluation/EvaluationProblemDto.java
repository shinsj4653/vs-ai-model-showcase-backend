package visang.showcase.aibackend.dto.response.evaluation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationProblemDto {
    private Integer q_idx; // 토픽 인덱스
    private Integer diff_level; // 난이도
    private Integer correct; // 정오답 여부 -> 디폴트 값 : 0
    private String prob_no; // 문항 번호
    private String topic_nm; // 토픽 한글명
    private String categ_cd; // 영역 코드
    private String categ_nm; // 영역 한글명
    private String chapter_nm; // 대단원 한글명
    private String section_nm; // 중단원 한글명
    private String subsection_nm; // 소단원 한글명
    private String imgpath; // 문제 이미지 url
    private String video_url; // 문제 학습 동영상 url
}