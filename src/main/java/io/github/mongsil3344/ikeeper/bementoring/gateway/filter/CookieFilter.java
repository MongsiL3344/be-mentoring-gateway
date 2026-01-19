package io.github.mongsil3344.ikeeper.bementoring.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import io.github.mongsil3344.ikeeper.bementoring.gateway.exception.ForbiddenException;

/**
 * 모든 요청에 대해 쿠키를 검증하는 webfilter
 * Globalfilter로 했다가 라우팅 되고 필터가 작동하는 구조라서 webfilter로 변경
 * webfilter를 사용하면 서블릿에서 필터가 실행돼서 
 * 라우트를 거치기 전에 모든 요청을 먼저 검증
 */
@Component
public class CookieFilter implements WebFilter, Ordered {

    private static final String ROLE_COOKIE_NAME = "ROLE";
    private static final String ROLE_HEADER_NAME = "X-Role";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        
        // 요청에서 ROLE 쿠키 조회
        HttpCookie roleCookie = exchange.getRequest().getCookies().getFirst(ROLE_COOKIE_NAME);

        // 쿠키가 없거나 값이 비어있으면 403 반환
        if (roleCookie == null || roleCookie.getValue().isBlank()) {
            return Mono.error(new ForbiddenException("ROLE cookie is required"));
        }

        // 쿠키의 ROLE 값이 user 혹은 admin이 아니면 403 반환
        String roleValue = roleCookie.getValue();
        if (!roleValue.equals("user") && !roleValue.equals("admin")) {
            return Mono.error(new ForbiddenException("ROLE cookie is invalid"));
        }

        // 검증 통과 - 라우팅에서 사용할 헤더로 전달
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(ROLE_HEADER_NAME, roleValue)
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100 + 1;
    }
}
