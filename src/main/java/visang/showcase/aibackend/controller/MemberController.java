package visang.showcase.aibackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import visang.showcase.aibackend.dto.request.member.MemberNoRequest;
import visang.showcase.aibackend.dto.response.common.ResponseDto;
import visang.showcase.aibackend.dto.response.common.ResponseUtil;
import visang.showcase.aibackend.dto.response.member.MemberDto;
import visang.showcase.aibackend.dto.response.token.TokenResponse;
import visang.showcase.aibackend.service.MemberService;

import javax.servlet.http.HttpServletRequest;
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

    @PostMapping("/setMemberNo")
    public ResponseDto<TokenResponse> setMemberNo(@RequestBody MemberNoRequest memberNoRequest) {

        TokenResponse result = memberService.setMemberNo(memberNoRequest);

        if (result == null) {
            return ResponseUtil.FAILURE("존재하지 않는 회원입니다.", null);
        } else {
            return ResponseUtil.SUCCESS("학생번호 선택 완료.", result);
        }
    }
}
