package org.example.flowmanager.api.security.context;

import lombok.Getter;
import org.example.flowmanager.api.utils.Utils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;

@Getter
@Component
@Scope(value = Utils.TYPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserContext {

    private final String login;

    public UserContext() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (Objects.nonNull(attributes)) {
            HttpServletRequest request = attributes.getRequest();
            this.login = request.getHeader(Utils.FIELD_USER_LOGIN);
        } else {
            this.login = Utils.NO_LOGIN;
        }
    }
}