package in.soaringeagles.api.registrations;

import in.soaringeagles.api.events.Event;
import in.soaringeagles.api.events.RegistrationType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RegistrationFieldValidator {

    public void validate(Event event, RegistrationRequest request) {
        if (event.getRegistrationType() == RegistrationType.INDIVIDUAL) {
            if (blank(request.name()) || blank(request.phone()) || blank(request.email())
                    || request.year() == null || blank(request.department()) || blank(request.institution())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete all individual registration fields.");
            }
            if (request.teamName() != null || request.captainName() != null || request.captainPhone() != null
                    || request.captainEmail() != null || request.members() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team details are not accepted for individual registration.");
            }
            return;
        }

        if (blank(request.teamName()) || blank(request.captainName()) || blank(request.captainPhone())
                || blank(request.captainEmail()) || request.members() == null
                || request.members().size() + 1 < event.getMinimumTeamSize()
                || request.members().size() + 1 > event.getMaximumTeamSize()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Enter the captain and the number of team members required for this team size.");
        }
        if (request.name() != null || request.phone() != null || request.email() != null || request.year() != null
                || request.department() != null || request.institution() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Individual details are not accepted for team registration.");
        }
        boolean invalidMember = request.members().stream().anyMatch(member ->
                blank(member.name()) || member.year() == null || blank(member.department())
                        || blank(member.institution()) || blank(member.email()));
        if (invalidMember) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Complete all team member fields.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
