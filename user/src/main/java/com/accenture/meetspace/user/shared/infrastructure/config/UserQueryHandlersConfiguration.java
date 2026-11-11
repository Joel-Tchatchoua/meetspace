package com.accenture.meetspace.user.shared.infrastructure.config;

import com.accenture.meetspace.bootstrap.application.query.PageResponse;
import com.accenture.meetspace.bootstrap.application.query.QueryHandlerRegistration;
import com.accenture.meetspace.user.profile.application.port.out.reader.UserReader;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.DirectoryEntry;
import com.accenture.meetspace.user.profile.application.usecases.query.profile.GetProfileQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.profile.GetProfileQueryHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.GetUserQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.GetUserQueryHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.user.ListUsersQueryHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQuery;
import com.accenture.meetspace.user.profile.application.usecases.query.directory.SearchDirectoryQueryHandler;
import com.accenture.meetspace.user.profile.application.usecases.query.user.UserView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserQueryHandlersConfiguration {

    @Bean
    QueryHandlerRegistration<GetProfileQuery, UserView> getProfile(UserReader userReader) {
        return new QueryHandlerRegistration<>(GetProfileQuery.class, new GetProfileQueryHandler(userReader));
    }

    @Bean
    QueryHandlerRegistration<GetUserQuery, UserView> getUser(UserReader userReader) {
        return new QueryHandlerRegistration<>(GetUserQuery.class, new GetUserQueryHandler(userReader));
    }

    @Bean
    QueryHandlerRegistration<ListUsersQuery, PageResponse<UserView>> listUsers(UserReader userReader) {
        return new QueryHandlerRegistration<>(ListUsersQuery.class, new ListUsersQueryHandler(userReader));
    }

    @Bean
    QueryHandlerRegistration<SearchDirectoryQuery, PageResponse<DirectoryEntry>> searchDirectory(
            UserReader userReader) {
        return new QueryHandlerRegistration<>(SearchDirectoryQuery.class,
                new SearchDirectoryQueryHandler(userReader));
    }
}
