package visang.showcase.aibackend.util;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*") // 적용할 URL 패턴을 지정합니다.
public class SessionFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("session start!!");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(true); // 세션이 없으면 새로 생성
        String requestUri = httpRequest.getRequestURI();

        // 세션에 memberNo 값이 없는 경우에 대한 처리 필요.
        if (!requestUri.startsWith("/members")
                && (session.getAttribute("memberNo") == null)) {

        } else {
            // 세션이 존재하고 "username" 속성이 있는 경우, 정상적인 처리
            chain.doFilter(request, response); // 다음 필터 또는 서블릿으로 요청 전달
        }
    }

    @Override
    public void destroy() {
        // Filter가 소멸될 때 필요한 정리 코드가 있다면 여기에 작성할 수 있습니다.
    }
}
