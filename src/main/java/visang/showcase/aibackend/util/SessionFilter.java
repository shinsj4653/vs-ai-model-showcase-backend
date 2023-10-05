package visang.showcase.aibackend.util;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true); // 세션이 없으면 새로 생성
        String requestUri = httpRequest.getRequestURI();

        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods","*");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader("Access-Control-Allow-Headers",
                "Origin, X-Requested-With, Content-Type, Accept, Authorization");

        // 세션에 memberNo 값이 없는 경우에 대한 처리 필요.
        if (!requestUri.startsWith("/members") &&
                !requestUri.equals("/")
                && (session.getAttribute("memberNo") == null)) {

            // Redirect to the root IP address
            httpResponse.sendRedirect("/"); // Change this to your desired root URL
            System.out.println("redirect to first page!!");
            return;

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
