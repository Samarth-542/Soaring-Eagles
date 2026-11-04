package in.soaringeagles.api.registrations;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RegistrationTeamMember {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "study_year", nullable = false)
    private int year;

    @Column(name = "department", nullable = false, length = 160)
    private String department;

    @Column(name = "institution", nullable = false, length = 200)
    private String institution;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    protected RegistrationTeamMember() {
    }

    RegistrationTeamMember(TeamMemberRequest request) {
        name = request.name().trim();
        year = request.year().value();
        department = request.department().trim();
        institution = request.institution().trim();
        email = request.email().trim();
    }

    public String getName() { return name; }
    public int getYear() { return year; }
    public String getDepartment() { return department; }
    public String getInstitution() { return institution; }
    public String getEmail() { return email; }
}
