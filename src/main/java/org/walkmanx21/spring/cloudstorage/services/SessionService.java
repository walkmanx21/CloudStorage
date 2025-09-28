package org.walkmanx21.spring.cloudstorage.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.walkmanx21.spring.cloudstorage.models.User;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final MyUserDetailService userDetailService;

    public void setSessionAttribute(User user) {
        HttpServletRequest request;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            UserDetails userDetails = userDetailService.loadUserByUsername(user.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        }

    }
}
