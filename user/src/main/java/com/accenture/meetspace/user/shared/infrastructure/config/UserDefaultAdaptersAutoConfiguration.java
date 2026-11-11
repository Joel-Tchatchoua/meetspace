package com.accenture.meetspace.user.shared.infrastructure.config;

import com.accenture.meetspace.user.profile.application.port.out.identity.IdentityProvider;
import com.accenture.meetspace.user.profile.application.port.out.reservation.ReservationCanceller;
import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.identity.UnavailableIdentityProvider;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.reservation.NoReservationCanceller;
import com.accenture.meetspace.user.profile.infrastructure.adapters.out.site.UnavailableSiteCatalog;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class UserDefaultAdaptersAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    IdentityProvider identityProvider() {
        return new UnavailableIdentityProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    SiteCatalog siteCatalog() {
        return new UnavailableSiteCatalog();
    }

    @Bean
    @ConditionalOnMissingBean
    ReservationCanceller reservationCanceller() {
        return new NoReservationCanceller();
    }
}
