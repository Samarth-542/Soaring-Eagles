package in.soaringeagles.api.registrations;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import in.soaringeagles.api.integrations.email.RegistrationEmailDeliveryRepository;
import in.soaringeagles.api.integrations.email.RegistrationEmailQueueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationAdminService {

    private final RegistrationRepository registrationRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final RegistrationFieldValidator fieldValidator;
    private final RegistrationEmailDeliveryRepository emailDeliveryRepository;
    private final RegistrationEmailQueueService emailQueueService;
    private final Clock clock;

    RegistrationAdminService(
            RegistrationRepository registrationRepository,
            PaymentAttemptRepository paymentAttemptRepository,
            RegistrationFieldValidator fieldValidator,
            RegistrationEmailDeliveryRepository emailDeliveryRepository,
            RegistrationEmailQueueService emailQueueService,
            Clock clock) {
        this.registrationRepository = registrationRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.fieldValidator = fieldValidator;
        this.emailDeliveryRepository = emailDeliveryRepository;
        this.emailQueueService = emailQueueService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<AdminRegistrationView> list(UUID eventId, int page, int size) {
        return registrationRepository.findAllByEventIdOrderByCreatedAtDesc(
                        eventId,
                        PageRequest.of(page, size))
                .map(this::toView);
    }

    @Transactional(readOnly = true)
    public AdminRegistrationView get(UUID registrationId) {
        return toView(findRegistration(registrationId));
    }

    @Transactional
    public AdminRegistrationView update(UUID registrationId, RegistrationRequest request) {
        Registration registration = registrationRepository.findByIdForUpdate(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
        fieldValidator.validate(registration.getEvent(), request);
        registration.updateDetails(request, clock.instant());
        return toView(registrationRepository.saveAndFlush(registration));
    }

    @Transactional
    public void retryConfirmationEmail(UUID registrationId) {
        findRegistration(registrationId);
        emailQueueService.retry(registrationId);
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv(UUID eventId) {
        List<Registration> registrations = registrationRepository.findAllByEventIdOrderByCreatedAtDesc(eventId);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Registration ID,Status,Type,Fee INR,Name,Phone,Email,Year,Department,Institution,")
                .append("Team,Captain,Team Members,Payment Status,Payment ID,Order ID,Created At\r\n");
        for (Registration registration : registrations) {
            PaymentAttempt attempt = paymentAttemptRepository
                    .findFirstByRegistrationIdOrderByAttemptNumberDesc(registration.getId())
                    .orElse(null);
            csv.append(cell(registration.getId().toString())).append(',')
                    .append(cell(registration.getState().name())).append(',')
                    .append(cell(registration.getRegistrationType().name())).append(',')
                    .append(cell(registration.getFeeInr().toPlainString())).append(',')
                    .append(cell(value(registration.getIndividualName(), registration.getCaptainName()))).append(',')
                    .append(cell(value(registration.getIndividualPhone(), registration.getCaptainPhone()))).append(',')
                    .append(cell(value(registration.getIndividualEmail(), registration.getCaptainEmail()))).append(',')
                    .append(cell(registration.getIndividualYear() == null ? "" : registration.getIndividualYear().toString())).append(',')
                    .append(cell(registration.getIndividualDepartment())).append(',')
                    .append(cell(registration.getIndividualInstitution())).append(',')
                    .append(cell(registration.getTeamName())).append(',')
                    .append(cell(registration.getCaptainName())).append(',')
                    .append(cell(memberSummary(registration.getMembers()))).append(',')
                    .append(cell(attempt == null ? "" : attempt.getState().name())).append(',')
                    .append(cell(attempt == null ? "" : attempt.getRazorpayPaymentId())).append(',')
                    .append(cell(attempt == null ? "" : attempt.getRazorpayOrderId())).append(',')
                    .append(cell(registration.getCreatedAt().toString()))
                    .append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private AdminRegistrationView toView(Registration registration) {
        PaymentAttempt attempt = paymentAttemptRepository
                .findFirstByRegistrationIdOrderByAttemptNumberDesc(registration.getId())
                .orElse(null);
        List<AdminTeamMemberView> members = registration.getMembers().stream()
                .map(member -> new AdminTeamMemberView(
                        member.getName(),
                        member.getYear(),
                        member.getDepartment(),
                        member.getInstitution(),
                        member.getEmail()))
                .toList();
        var emailDelivery = emailDeliveryRepository.findByRegistrationId(registration.getId()).orElse(null);
        return new AdminRegistrationView(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getRegistrationType(),
                registration.getState(),
                registration.getFeeInr(),
                registration.getIndividualName(),
                registration.getIndividualPhone(),
                registration.getIndividualEmail(),
                registration.getIndividualYear(),
                registration.getIndividualDepartment(),
                registration.getIndividualInstitution(),
                registration.getTeamName(),
                registration.getCaptainName(),
                registration.getCaptainPhone(),
                registration.getCaptainEmail(),
                members,
                attempt == null ? null : attempt.getState(),
                emailDelivery == null ? null : emailDelivery.getState(),
                emailDelivery == null ? 0 : emailDelivery.getAttemptCount(),
                emailDelivery == null ? null : emailDelivery.getLastError(),
                attempt == null ? null : attempt.getRazorpayOrderId(),
                attempt == null ? null : attempt.getRazorpayPaymentId(),
                registration.getHoldExpiresAt(),
                registration.getCreatedAt(),
                registration.getUpdatedAt());
    }

    private Registration findRegistration(UUID registrationId) {
        return registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found."));
    }

    private String memberSummary(List<RegistrationTeamMember> members) {
        return members.stream()
                .map(member -> member.getName() + " (" + member.getEmail() + ", year " + member.getYear() + ")")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String value(String primary, String secondary) {
        return primary == null ? secondary : primary;
    }

    private String cell(String value) {
        String safe = value == null ? "" : value;
        if (safe.matches("^[\\s]*[=+@\\-\\t\\r].*")) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
