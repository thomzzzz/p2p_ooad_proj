package com.p2p.filter;

import com.p2p.model.User;
import com.p2p.service.PeerRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class PeerRegistrationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(PeerRegistrationFilter.class);
    
    private PeerRegistrationService peerRegistrationService;
    
    @Autowired
    public PeerRegistrationFilter(PeerRegistrationService peerRegistrationService) {
        this.peerRegistrationService = peerRegistrationService;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Get user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof User) {
                
                User user = (User) authentication.getPrincipal();
                String userId = user.getId();
                
                // Only register peer for API calls (not for resources, etc.)
                String path = request.getRequestURI();
                if (path.startsWith("/api/") && !path.equals("/api/auth/logout")) {
                    peerRegistrationService.registerPeer(userId, request);
                }
                
                // For logout, mark peer as offline
                if (path.equals("/api/auth/logout")) {
                    peerRegistrationService.updatePeerStatus(userId, false);
                }
            }
        } catch (Exception e) {
            logger.error("Error in peer registration filter", e);
            // We still continue the filter chain even if peer registration fails
        }
        
        filterChain.doFilter(request, response);
    }
}