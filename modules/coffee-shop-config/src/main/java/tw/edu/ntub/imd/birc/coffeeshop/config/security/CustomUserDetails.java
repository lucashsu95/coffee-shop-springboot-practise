package tw.edu.ntub.imd.birc.coffeeshop.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final String email;
    private final String cName;
    private final boolean isEnabled;

    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String email, String cName, boolean isEnabled, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.cName = cName;
        this.isEnabled = isEnabled;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getCName() {
        return cName;
    }
}


