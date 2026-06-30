package crm.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import crm.builder.ContactBuilder;
import crm.builder.OpportunityBuilder;
import crm.facade.CrmFacade;
import crm.factory.ActivityFactory;
import crm.model.entity.*;
import crm.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console UI - interfață simplă în terminal pentru demonstrație.
 * 
 * Demonstrează utilizarea tuturor pattern-urilor din proiect prin
 * FAȚADA CrmFacade - punct unic de intrare.
 */
public class ConsoleUI {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);

    private final CrmFacade crm;
    private final Scanner scanner;
    private boolean running = true;

    public ConsoleUI() {
        this.crm = CrmFacade.getInstance();
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        printBanner();
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            handleChoice(choice);
        }
    }

    private void printBanner() {
        System.out.println("\n=================================================");
        System.out.println("    CRM TRAINING IT - Sistem de Management");
        System.out.println("    Versiunea: 1.0.0  |  Java 8 + JDBC");
        System.out.println("=================================================\n");
    }

    private void printMenu() {
        System.out.println("\n--- MENIU PRINCIPAL ---");
        System.out.println("1.  Adaugă lead individual (B2C)");
        System.out.println("2.  Adaugă client corporate (B2B)");
        System.out.println("3.  Caută contact după email");
        System.out.println("4.  Listează lead-uri fierbinți");
        System.out.println("5.  Schimbă status lead");
        System.out.println("6.  Adaugă activitate (apel/email)");
        System.out.println("7.  Creează oportunitate B2B");
        System.out.println("8.  Mutare oportunitate în pipeline");
        System.out.println("9.  Vizualizare pipeline activ");
        System.out.println("10. Statistici contacte");
        System.out.println("11. Listează cursuri active");
        System.out.println("12. Demo rapid (date de test)");
        System.out.println("0.  Ieșire");
        System.out.print("\nAlegerea ta: ");
    }

    private void handleChoice(String choice) {
        try {
            switch (choice) {
                case "1": addIndividualLead(); break;
                case "2": addCorporateClient(); break;
                case "3": searchByEmail(); break;
                case "4": listHotLeads(); break;
                case "5": changeLeadStatus(); break;
                case "6": addActivity(); break;
                case "7": createOpportunity(); break;
                case "8": moveOpportunityStage(); break;
                case "9": showPipeline(); break;
                case "10": showStatistics(); break;
                case "11": listCourses(); break;
                case "12": runDemo(); break;
                case "0": running = false; System.out.println("La revedere!"); break;
                default: System.out.println("Opțiune invalidă!");
            }
        } catch (Exception e) {
            System.out.println("EROARE: " + e.getMessage());
            logger.error("Eroare în UI", e);
        }
    }

    // =====================================================
    // METODE INDIVIDUALE
    // =====================================================

    private void addIndividualLead() {
        System.out.println("\n--- Adăugare Lead Individual ---");
        String firstName = prompt("Prenume: ");
        String lastName = prompt("Nume: ");
        String email = prompt("Email: ");
        String phone = prompt("Telefon: ");

        // BUILDER PATTERN
        Contact contact = new ContactBuilder()
                .asIndividual()
                .name(firstName, lastName)
                .email(email)
                .phone(phone)
                .leadSource(LeadSource.WEBSITE)
                .experienceLevel(ExperienceLevel.BEGINNER)
                .learningGoal("Reconversie profesională")
                .withGdprConsent()
                .build();

        // FACADE PATTERN - operație simplă
        Contact saved = crm.createContact(contact);
        System.out.println("Contact creat cu ID: " + saved.getId() + ", scor: " + saved.getLeadScore());
    }

    private void addCorporateClient() {
        System.out.println("\n--- Adăugare Client Corporate ---");
        String companyName = prompt("Nume companie: ");
        String email = prompt("Email principal: ");
        String fiscalCode = prompt("Cod fiscal: ");
        String industry = prompt("Industrie: ");
        int employees = Integer.parseInt(prompt("Număr angajați: "));

        Contact contact = new ContactBuilder()
                .asCorporate()
                .companyName(companyName)
                .email(email)
                .fiscalDetails(fiscalCode, null)
                .industry(industry, employees)
                .leadSource(LeadSource.REFERRAL)
                .withGdprConsent()
                .build();

        Contact saved = crm.createContact(contact);
        System.out.println("Client corporate creat: " + saved.getId() + ", scor: " + saved.getLeadScore());
    }

    private void searchByEmail() {
        String email = prompt("Email: ");
        Optional<Contact> result = crm.findContactByEmail(email);
        if (result.isPresent()) {
            Contact c = result.get();
            System.out.println("Contact găsit:");
            System.out.println("  ID: " + c.getId());
            System.out.println("  Nume: " + c.getFullName().orElse("N/A"));
            System.out.println("  Tip: " + c.getContactType());
            System.out.println("  Status: " + c.getLeadStatus());
            System.out.println("  Scor lead: " + c.getLeadScore());
        } else {
            System.out.println("Contact negăsit.");
        }
    }

    private void listHotLeads() {
        int limit = Integer.parseInt(prompt("Câte lead-uri (default 10): "));
        List<Contact> hot = crm.getHotLeads(limit);
        System.out.println("\n--- Lead-uri Fierbinți ---");
        hot.forEach(c -> System.out.printf("  [%d] %s | scor: %d | status: %s%n",
                c.getId(), c.getFullName().orElse("?"), c.getLeadScore(), c.getLeadStatus()));
        if (hot.isEmpty()) System.out.println("(niciun lead fierbinte)");
    }

    private void changeLeadStatus() {
        long id = Long.parseLong(prompt("Contact ID: "));
        System.out.println("Statusuri: NEW, CONTACTED, INTERESTED, QUALIFIED, ENROLLED, LOST");
        String status = prompt("Nou status: ");
        crm.changeLeadStatus(id, LeadStatus.valueOf(status.toUpperCase()));
        System.out.println("Status schimbat cu succes.");
    }

    private void addActivity() {
        long contactId = Long.parseLong(prompt("Contact ID: "));
        System.out.println("Tipuri: EMAIL, CALL, MEETING, NOTE");
        String type = prompt("Tip activitate: ").toUpperCase();
        String subject = prompt("Subiect: ");

        Activity activity;
        switch (type) {
            case "EMAIL":
                activity = ActivityFactory.createEmail(contactId, subject, "Email manual", 1L);
                break;
            case "CALL":
                activity = ActivityFactory.createCall(contactId, subject,
                        java.time.LocalDateTime.now().plusHours(1), 1L);
                break;
            case "MEETING":
                activity = ActivityFactory.createMeeting(contactId, null, subject,
                        java.time.LocalDateTime.now().plusDays(1), 60, 1L);
                break;
            case "NOTE":
                activity = ActivityFactory.createNote(contactId, subject, prompt("Conținut: "), 1L);
                break;
            default:
                System.out.println("Tip invalid.");
                return;
        }

        Activity saved = crm.logActivity(activity);
        System.out.println("Activitate creată cu ID: " + saved.getId());
    }

    private void createOpportunity() {
        long clientId = Long.parseLong(prompt("Client ID: "));
        String title = prompt("Titlu oportunitate: ");
        int participants = Integer.parseInt(prompt("Estimat participanți: "));
        BigDecimal value = new BigDecimal(prompt("Valoare estimată (RON): "));

        Opportunity opp = new OpportunityBuilder()
                .forClient(clientId)
                .title(title)
                .participants(participants)
                .estimatedValue(value)
                .deliveryMode(DeliveryMode.ON_SITE)
                .stage(OpportunityStage.LEAD_QUALIFICATION)
                .expectedClose(LocalDate.now().plusMonths(2))
                .build();

        Opportunity saved = crm.createOpportunity(opp);
        System.out.println("Oportunitate creată: " + saved.getId());
    }

    private void moveOpportunityStage() {
        long id = Long.parseLong(prompt("Oportunitate ID: "));
        System.out.println("Etape: LEAD_QUALIFICATION, NEEDS_ANALYSIS, PROPOSAL_SENT, " +
                "NEGOTIATION, CONTRACT_REVIEW, WON, LOST");
        String stage = prompt("Nouă etapă: ");
        crm.moveOpportunityStage(id, OpportunityStage.valueOf(stage.toUpperCase()));
        System.out.println("Etapă schimbată.");
    }

    private void showPipeline() {
        System.out.println("\n--- Pipeline Activ ---");
        List<Opportunity> pipeline = crm.getActivePipeline();
        pipeline.forEach(o -> System.out.printf("  [%d] %s | %s | %s RON | %d%%%n",
                o.getId(), o.getTitle(), o.getStage(),
                o.getEstimatedValue(), o.getProbabilityPercent()));
        if (pipeline.isEmpty()) System.out.println("(pipeline gol)");
    }

    private void showStatistics() {
        System.out.println("\n--- Statistici Contacte ---");
        System.out.println("Total contacte: " + crm.getTotalContacts());
        for (LeadStatus status : LeadStatus.values()) {
            System.out.printf("  %s: %d%n", status, crm.getContactsByStatus(status));
        }
    }

    private void listCourses() {
        System.out.println("\n--- Cursuri Active ---");
        List<Course> courses = crm.getActiveCourses();
        courses.forEach(c -> System.out.printf("  [%s] %s | %s | %d ore%n",
                c.getCode(), c.getName(), c.getCategory(),
                c.getDurationHours()));
        if (courses.isEmpty()) System.out.println("(niciun curs)");
    }

    private void runDemo() {
        System.out.println("\n--- Rulare Demo ---");

        // Creare lead B2C
        Contact lead = new ContactBuilder()
                .asIndividual()
                .name("Maria", "Demo")
                .email("maria.demo." + System.currentTimeMillis() + "@example.com")
                .phone("0712345678")
                .leadSource(LeadSource.WEBSITE)
                .experienceLevel(ExperienceLevel.BEGINNER)
                .learningGoal("Reconversie profesională în IT")
                .withGdprConsent()
                .withMarketingConsent()
                .build();
        Contact saved = crm.createContact(lead);
        System.out.println("Lead B2C creat: ID=" + saved.getId() + " scor=" + saved.getLeadScore());

        // Adăugare activitate
        Activity activity = ActivityFactory.createCall(saved.getId(),
                "Consultare inițială", java.time.LocalDateTime.now().plusDays(1), 1L);
        Activity act = crm.logActivity(activity);
        System.out.println("Activitate adăugată: ID=" + act.getId());

        // Finalizare activitate (declanșează update scor)
        crm.completeActivity(act.getId(), "Persoana este interesată", "Trimite ofertă");

        // Schimbare status
        crm.changeLeadStatus(saved.getId(), LeadStatus.INTERESTED);

        // Client corporate
        Contact corp = new ContactBuilder()
                .asCorporate()
                .companyName("TechCorp SRL Demo")
                .email("contact.demo." + System.currentTimeMillis() + "@techcorp.ro")
                .phone("0212345678")
                .fiscalDetails("RO12345678", "J40/1234/2020")
                .industry("IT & Software", 150)
                .leadSource(LeadSource.REFERRAL)
                .withGdprConsent()
                .build();
        Contact savedCorp = crm.createContact(corp);
        System.out.println("Client B2B creat: ID=" + savedCorp.getId() + " scor=" + savedCorp.getLeadScore());

        // Oportunitate B2B
        Opportunity opp = new OpportunityBuilder()
                .forClient(savedCorp.getId())
                .title("Training Java pentru echipa dev")
                .participants(20)
                .estimatedValue(new BigDecimal("25000"))
                .deliveryMode(DeliveryMode.ON_SITE)
                .stage(OpportunityStage.NEEDS_ANALYSIS)
                .expectedClose(LocalDate.now().plusMonths(1))
                .build();
        Opportunity savedOpp = crm.createOpportunity(opp);
        System.out.println("Oportunitate creată: ID=" + savedOpp.getId());

        // Mutare în etape
        crm.moveOpportunityStage(savedOpp.getId(), OpportunityStage.PROPOSAL_SENT);

        System.out.println("\nDemo finalizat cu succes!");
    }

    private String prompt(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }
}
