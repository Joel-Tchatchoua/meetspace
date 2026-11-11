package com.accenture.meetspace.user.profile.application.usecases.command.updateprofile;

import com.accenture.meetspace.bootstrap.application.command.CommandHandler;
import com.accenture.meetspace.user.profile.application.port.out.repository.UserRepository;
import com.accenture.meetspace.user.profile.application.port.out.site.SiteCatalog;
import com.accenture.meetspace.user.profile.domain.exceptions.DefaultSiteNotFoundException;
import com.accenture.meetspace.user.profile.domain.exceptions.UserNotFoundException;

public class UpdateProfileCommandHandler implements CommandHandler<UpdateProfileCommand, UpdateProfileResponse> {

    private final UserRepository userRepository;
    private final SiteCatalog siteCatalog;

    public UpdateProfileCommandHandler(UserRepository userRepository, SiteCatalog siteCatalog) {
        this.userRepository = userRepository;
        this.siteCatalog = siteCatalog;
    }

    @Override
    public UpdateProfileResponse handle(UpdateProfileCommand command) {
        var user = userRepository.findUser(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (command.defaultSiteId() != null && !siteCatalog.exists(command.defaultSiteId())) {
            throw new DefaultSiteNotFoundException(command.defaultSiteId());
        }
        user.changeProfile(command.firstName(), command.lastName(), command.department(), command.phone(),
                command.defaultSiteId());
        userRepository.save(user);
        return new UpdateProfileResponse(command.userId());
    }
}
