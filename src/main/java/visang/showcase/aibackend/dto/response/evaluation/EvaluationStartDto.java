package visang.showcase.aibackend.dto.response.evaluation;

import lombok.*;

import java.util.List;

@Data
public class EvaluationStartDto {
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

    private String transaction_token;
    private Double knowledge_rate;

    public EvaluationStartDto(EvaluationProblemDto dto, String token, Double knowledgeRate) {
        this.q_idx = dto.getQ_idx();
        this.diff_level = dto.getDiff_level();
        this.correct = dto.getCorrect();
        this.prob_no = dto.getProb_no();
        this.topic_nm = dto.getTopic_nm();
        this.categ_cd = dto.getCateg_cd();
        this.categ_nm = dto.getCateg_nm();
        this.chapter_nm = dto.getChapter_nm();
        this.section_nm = dto.getSection_nm();
        this.subsection_nm = dto.getSubsection_nm();
        this.imgpath = dto.getImgpath();
        this.video_url = dto.getVideo_url();

        this.transaction_token = token;
        this.knowledge_rate = knowledgeRate;
    }
}