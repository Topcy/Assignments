package tests;

import config.TestConfig;
import models.BookingPayload;
import models.CustomerDetails;
import pages.BookingCard;
import pages.BookingDetailsPage;
import pages.EventDetailsPage;
import pages.EventsPage;
import pages.LoginPage;
import pages.MyBookingsPage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class BookingFlowTest extends BaseTest {

    private static final CustomerDetails CUSTOMER =
            new CustomerDetails("Teepee", "test123@gmail.com", "+91 98765 43210");

    private LoginPage loginPage;
    private EventsPage eventsPage;
    private EventDetailsPage eventDetailsPage;
    private MyBookingsPage myBookingsPage;
    private BookingDetailsPage bookingDetailsPage;

    @BeforeMethod(alwaysRun = true)
    public void createPageObjects() {
        loginPage = new LoginPage(page);
        eventsPage = new EventsPage(page);
        eventDetailsPage = new EventDetailsPage(page);
        myBookingsPage = new MyBookingsPage(page);
        bookingDetailsPage = new BookingDetailsPage(page);
    }

    @Test(description = "Two distinct bookings appear correctly in My Bookings and on their own detail pages")
    public void twoBookingsAreListedAndDetailedWithoutMixUps() {

        // login
        loginPage.open().loginAs(TestConfig.USER_EMAIL, TestConfig.USER_PASSWORD);

        // booking 1: World Tech Summit
        eventsPage.open();
        eventsPage.filterBy("Conference", "Hyderabad", "World").openOnlyResult();
        BookingPayload first = eventDetailsPage.bookTickets(1, CUSTOMER);

        assertEquals(first.eventTitle(), "World Tech Summit", "first booking: event title");
        assertFalse(first.reference().isBlank(), "first booking: reference must not be empty");
        assertEquals(first.tickets(), 1, "first booking: ticket count");
        assertFalse(first.total().isBlank(), "first booking: total text must not be empty");
        assertEquals(first.customerEmail(), CUSTOMER.email(), "first booking: customer email");

        // booking 2: Dilli Diwali Mela
        eventsPage.open();
        eventsPage.filterBy("Festival", "Delhi", "Dilli").openOnlyResult();
        BookingPayload second = eventDetailsPage.bookTickets(2, CUSTOMER);

        assertNotEquals(second.reference(), first.reference(), "bookings must have different references");
        assertNotEquals(second.eventTitle(), first.eventTitle(), "bookings must have different event titles");
        assertEquals(second.tickets(), 2, "second booking: ticket count");

        // My Bookings: find cards by reference
        myBookingsPage.open();
        BookingCard firstCard = myBookingsPage.cardForReference(first.reference());
        BookingCard secondCard = myBookingsPage.cardForReference(second.reference());

        String firstBookingId = assertCardMatchesPayload(firstCard, first);
        String secondBookingId = assertCardMatchesPayload(secondCard, second);
        assertNotEquals(firstBookingId, secondBookingId, "each card must have its own booking id");

        // first booking: detail page
        firstCard.openDetails();
        assertDetailsMatchPayload(first, firstBookingId);

        // back to the list, re-find the second booking by reference
        bookingDetailsPage.backToMyBookings();
        BookingCard secondCardAgain = myBookingsPage.cardForReference(second.reference());
        assertThat(secondCardAgain.root()).hasCount(1);
        secondCardAgain.openDetails();

        assertDetailsMatchPayload(second, secondBookingId);

        // The first booking's reference must not leak onto the second booking's page.
        assertThat(bookingDetailsPage.mentionsOf(first.reference())).hasCount(0);
    }

    private String assertCardMatchesPayload(BookingCard card, BookingPayload expected) {
        // Exactly one card contains this reference, and the user can see it.
        assertThat(card.root()).hasCount(1);
        assertThat(card.root()).isVisible();

        assertThat(card.status()).hasText(Pattern.compile("confirmed", Pattern.CASE_INSENSITIVE));
        assertThat(card.title()).hasText(expected.eventTitle());
        assertThat(card.ticketCount()).containsText(expected.ticketText());
        assertThat(card.total()).hasText(expected.total());

        assertThat(card.bookingId()).hasText(Pattern.compile("^#\\d+$"));
        return card.bookingId().innerText().trim();
    }

    // Verifies the Booking Details page that is currently open against one stored payload.
    private void assertDetailsMatchPayload(BookingPayload expected, String bookingIdFromList) {
        // The URL ends with the numeric id
        String numericId = bookingIdFromList.substring(1);
        assertThat(page).hasURL(Pattern.compile(".*/bookings/" + numericId + "$"));

        assertThat(bookingDetailsPage.title()).hasText(expected.eventTitle());

        // reference shown both in the breadcrumb and in the page header
        assertThat(bookingDetailsPage.breadcrumb()).containsText(expected.reference());
        assertThat(bookingDetailsPage.headerReference()).hasText(expected.reference());

        assertThat(bookingDetailsPage.customerEmail()).hasText(expected.customerEmail());
        assertThat(bookingDetailsPage.paymentTickets()).hasText(String.valueOf(expected.tickets()));
        assertThat(bookingDetailsPage.paymentTotal()).hasText(expected.total());

        // booking id: numeric, and identical to the one on the list card
        assertThat(bookingDetailsPage.bookingId()).hasText(Pattern.compile("^#\\d+$"));
        assertThat(bookingDetailsPage.bookingId()).hasText(bookingIdFromList);
    }
}
