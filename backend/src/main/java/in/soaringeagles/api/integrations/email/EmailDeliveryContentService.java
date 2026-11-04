package in.soaringeagles.api.integrations.email;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.stream.Collectors;

import in.soaringeagles.api.registrations.PaymentAttemptRepository;
import in.soaringeagles.api.registrations.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailDeliveryContentService {

    private static final DateTimeFormatter EVENT_DATE =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' h:mm a").withZone(ZoneId.of("Asia/Kolkata"));

    private final RegistrationEmailDeliveryRepository deliveryRepository;
    private final RegistrationRepository registrationRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    EmailDeliveryContentService(
            RegistrationEmailDeliveryRepository deliveryRepository,
            RegistrationRepository registrationRepository,
            PaymentAttemptRepository paymentAttemptRepository) {
        this.deliveryRepository = deliveryRepository;
        this.registrationRepository = registrationRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Transactional(readOnly = true)
    public EmailMessage buildMessage(java.util.UUID deliveryId) {
        RegistrationEmailDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalStateException("Email delivery disappeared."));
        var registration = registrationRepository.findById(delivery.getRegistration().getId())
                .orElseThrow(() -> new IllegalStateException("Registration for email delivery disappeared."));
        var event = registration.getEvent();
        String recipient = registration.getRegistrationType().name().equals("TEAM")
                ? registration.getCaptainEmail()
                : registration.getIndividualEmail();
        var payment = paymentAttemptRepository
                .findFirstByRegistrationIdOrderByAttemptNumberDesc(registration.getId())
                .orElse(null);
        String participant = registration.getRegistrationType().name().equals("TEAM")
                ? "Team: " + registration.getTeamName() + "\nCaptain: " + registration.getCaptainName()
                        + "\nMembers:\n" + registration.getMembers().stream()
                                .map(member -> "- " + member.getName() + " (" + member.getEmail() + ")")
                                .collect(Collectors.joining("\n"))
                : "Participant: " + registration.getIndividualName();
        String paymentInfo = payment == null
                ? "Registration fee: INR 0.00 (no payment required)"
                : "Registration fee: INR " + registration.getFeeInr().toPlainString()
                        + "\nPayment reference: " + payment.getRazorpayPaymentId();
        String text = """
                Your registration is confirmed.

                Event: %s
                Starts: %s
                Ends: %s
                Venue: %s

                %s

                %s

                Keep this email as your payment confirmation. Payment reference is not a tax invoice.
                Soaring Eagles
                """.formatted(
                event.getTitle(),
                EVENT_DATE.format(event.getStartsAt().toInstant()),
                EVENT_DATE.format(event.getEndsAt().toInstant()),
                event.getVenue(),
                participant,
                paymentInfo);
        return new EmailMessage(recipient, "Registration confirmed — " + event.getTitle(), text);
    }
}
