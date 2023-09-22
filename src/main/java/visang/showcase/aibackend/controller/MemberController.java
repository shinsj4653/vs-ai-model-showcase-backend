package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.member.MemberDto;
import visang.showcase.aibackend.service.MemberService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("members")
public class MemberController {

    private final MemberService memberService;
    @GetMapping
    public ResponseDto<List<MemberDto>> getMembers() {
        return ResponseUtil.SUCCESS("샘플 학생 데이터 및 타켓 토픽 조회 성공", memberService.getMembers());
    }
}
