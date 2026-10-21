package com.accenture.meetspace.shared.infrastructure.bus;

import com.accenture.meetspace.shared.application.query.InvalidQueryException;
import com.accenture.meetspace.shared.application.query.PaginatedQuery;
import com.accenture.meetspace.shared.application.query.Query;
import com.accenture.meetspace.shared.application.query.QueryBus;
import com.accenture.meetspace.shared.application.query.QueryHandlerRegistration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class QueryHandlerBus implements QueryBus {

    private final Map<Class<?>, QueryHandlerRegistration<?, ?>> registrations;
    private final TransactionTemplate transactionTemplate;

    public QueryHandlerBus(List<QueryHandlerRegistration<?, ?>> registrations,
                           PlatformTransactionManager transactionManager) {
        this.registrations = registrations.stream()
                .collect(Collectors.toUnmodifiableMap(
                        QueryHandlerRegistration::queryType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Several handlers registered for " + first.queryType().getName());
                        }));
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R ask(Query<R> query) {
        var registration = registrationFor(query);
        if (query instanceof PaginatedQuery<?> paginated) {
            checkSort(paginated);
        }
        return transactionTemplate.execute(status -> (R) registration.handle(query));
    }

    private static void checkSort(PaginatedQuery<?> query) {
        query.pagination().sort().stream()
                .filter(order -> !query.sortableColumns().containsKey(order.property()))
                .findFirst()
                .ifPresent(order -> {
                    throw new InvalidQueryException("sort", "cannot sort by " + order.property()
                            + ", expected one of " + new TreeSet<>(query.sortableColumns().keySet()));
                });
    }

    private QueryHandlerRegistration<?, ?> registrationFor(Query<?> query) {
        var registration = registrations.get(query.getClass());
        if (registration == null) {
            throw new IllegalStateException("No handler registered for " + query.getClass().getName());
        }
        return registration;
    }
}
