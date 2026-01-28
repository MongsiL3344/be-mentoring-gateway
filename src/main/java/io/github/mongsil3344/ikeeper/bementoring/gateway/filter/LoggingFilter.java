package io.github.mongsil3344.ikeeper.bementoring.gateway.filter;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 게이트웨이로 들어오는 모든 요청의 
 * HTTP 메서드와 URL과 ROLE 쿠키 값을 
 * 로그로 출력하는 필터
 */
@Slf4j
@Component
public class LoggingFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String method = String.valueOf(exchange.getRequest().getMethod());
        String path = exchange.getRequest().getPath().value();
        
        /*
         * 앞선 필터에서 주입한 헤더 값을 우선 사용하고,
         * 없으면 쿠키를 확인해서 로깅
         */
        String role = exchange.getRequest().getHeaders().getFirst("X-Role");
        if (role == null) {
            role = exchange.getRequest().getCookies().getFirst("ROLE") != null
                    ? exchange.getRequest().getCookies().getFirst("ROLE").getValue()
                    : "-";
        }
        final String roleValue = role;
        
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    URI requestUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                    String target = "-";
                    if (requestUrl != null) {
                        if (requestUrl.getPort() > -1) {
                            target = requestUrl.getHost() + ":" + requestUrl.getPort();
                        } else {
                            target = requestUrl.toString();
                        }
                    }
                    log.info("\n[Gateway Request]\nMethod: {}\nUrl: {}\nRole: {}\nTarget: {}\n", method, path, roleValue, target);
                }));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
