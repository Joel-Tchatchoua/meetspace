package com.accenture.meetspace.shared.infrastructure.bus;

import com.accenture.meetspace.shared.application.command.Command;
import com.accenture.meetspace.shared.application.command.CommandBus;
import com.accenture.meetspace.shared.application.command.CommandHandlerRegistration;
import com.accenture.meetspace.shared.application.command.ConcurrentUpdateException;
import com.accenture.meetspace.shared.application.command.TransactionalException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandHandlerBus implements CommandBus {

    private final Map<Class<?>, CommandHandlerRegistration<?, ?>> registrations;
    private final TransactionTemplate transactionTemplate;

    public CommandHandlerBus(List<CommandHandlerRegistration<?, ?>> registrations,
                             PlatformTransactionManager transactionManager) {
        this.registrations = registrations.stream()
                .collect(Collectors.toUnmodifiableMap(
                        CommandHandlerRegistration::commandType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Several handlers registered for " + first.commandType().getName());
                        }));
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R dispatch(Command<R> command) {
        var registration = registrationFor(command);
        try {
            return transactionTemplate.execute(status -> (R) registration.handle(command));
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrentUpdateException(command, e);
        } catch (TransactionException | DataAccessException e) {
            throw new TransactionalException(command, e);
        }
    }

    private CommandHandlerRegistration<?, ?> registrationFor(Command<?> command) {
        var registration = registrations.get(command.getClass());
        if (registration == null) {
            throw new IllegalStateException("No handler registered for " + command.getClass().getName());
        }
        return registration;
    }
}
