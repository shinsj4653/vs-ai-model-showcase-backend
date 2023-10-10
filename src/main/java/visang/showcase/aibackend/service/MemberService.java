package visang.showcase.aibackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import visang.showcase.aibackend.dto.request.member.MemberNoRequest;
import visang.showcase.aibackend.dto.response.member.MemberDto;
import visang.showcase.aibackend.dto.response.member.MemberQueryDto;
import visang.showcase.aibackend.dto.response.token.TokenResponse;
import visang.showcase.aibackend.mapper.MemberMapper;
import visang.showcase.aibackend.util.JwtTokenProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final JwtTokenProvider tokenProvider;
    public List<MemberDto> getMembers() {
        List<MemberQueryDto> members = memberMapper.getMembers();
        return members.stream()
                .map(MemberDto::of)
                .collect(Collectors.toList());
    }

    public TokenResponse setMemberNo(MemberNoRequest memberNoRequest) {
        // 프론트엔드에서 전달한 member_no 값을 가져옴
        String memberNo = memberNoRequest.getMemberNo();

        Map<String, Object> claims = new HashMap<>();
        claims.put("memberNo", memberNo);
        String token = tokenProvider.createToken(claims);

        // transaction_data 테이블 내에 유저 토큰 값 insert
        memberMapper.saveToken(token);

        // memberNo 저장한 토큰 값을 return
        return new TokenResponse(token);
    }
}
