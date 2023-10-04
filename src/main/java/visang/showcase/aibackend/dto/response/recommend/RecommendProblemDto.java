package visang.showcase.aibackend.dto.response.recommend;

import lombok.Getter;

@Getter
public class RecommendProblemDto {
    private Integer q_idx; // 토픽 인덱스
    private String prob_no; // 문항 번호
    private String prob_diff; // 문항 난이도
    private String topic_nm; // 토픽 한글명
    private String categ_cd; // 영역 코드
    private String categ_nm; // 영역 한글명
    private String chapter_nm; // 대단원 한글명
    private String section_nm; // 중단원 한글명
    private String subsection_nm; // 소단원 한글명
    private String imgpath; // 문제 이미지 url
    private String video_url; // 문제 동영상 url
}