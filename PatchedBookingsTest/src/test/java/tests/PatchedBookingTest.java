package tests;

import config.TestConfig;
import pages.BookingCard;
import pages.BookingDetailPage;
import pages.BookingsPage;
import pages.LoginPage;
import pages.NavBar;
import utils.BookingApiInterceptor;
import utils.BookingSnapshot;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;


public class PatchedBookingTest extends BaseTest {

    @Test(description = "One patched booking stays consistent from list to detail and back to list, "
            + "while every other booking and the customer e-mail stay live")
    public void patchedBookingStaysConsistent() {

        LoginPage loginPage = new LoginPage(page);
        NavBar navBar = new NavBar(page);
        BookingsPage bookingsPage = new BookingsPage(page);
        BookingDetailPage detailPage = new BookingDetailPage(page);
        BookingApiInterceptor api = new BookingApiInterceptor(page);


        // Log in
        loginPage.open();
        assertThat(loginPage.signInHeading()).isVisible();
        loginPage.loginAs(TestConfig.email(), TestConfig.password());

        assertThat(navBar.userEmail()).hasText(TestConfig.email());

        // My Bookings navigation is available
        assertThat(navBar.myBookingsLink()).isVisible();
        assertThat(navBar.myBookingsLink()).isEnabled();
        assertThat(navBar.myBookingsLink()).hasAttribute("href", "/bookings");

        // Intercept the list response before opening My Bookings
        api.patchBookingsList();

        // Open My Bookings
        navBar.openMyBookings();
        assertThat(bookingsPage.heading()).isVisible();

        assertThat(bookingsPage.cards()).not().hasCount(0);

        // Preconditions about the live data.
        assertTrue(api.listWasIntercepted(), "The list API call was not intercepted");
        assertTrue(api.liveBookingCount() >= 2,
                "Need at least 2 live bookings to prove 'only one is patched', found " + api.liveBookingCount()
                        + ". Create bookings for " + TestConfig.email() + " first.");

        BookingSnapshot original = api.originalBooking();   // live values of the booking we patched
        BookingSnapshot patched = api.patchedBooking();      // the patched object, stored for later checks

        // Exactly one record altered in the API response
        assertEquals(api.changedRecordCount(), 1, "Exactly one booking record must be altered in the list response");
        assertEquals(patched.id(), original.id(), "The patched booking must keep its original id");
        assertNotEquals(patched.reference(), original.reference());
        assertEquals(patched.reference(), BookingApiInterceptor.PATCHED_REFERENCE);

        // Locate the patched card by reference text and verify its content
        BookingCard patchedCard = bookingsPage.cardByReference(patched.reference());

        assertThat(patchedCard.root()).hasCount(1);
        assertThat(patchedCard.title()).hasText(patched.title());
        assertThat(patchedCard.tickets(patched.tickets())).isVisible();
        assertThat(patchedCard.total(patched.displayTotal())).isVisible();
        assertThat(patchedCard.bookingId()).hasText("#" + patched.id());

        // Prove only one card was altered: the others still show their live data
        assertThat(bookingsPage.cards()).hasCount(api.liveBookingCount());
        assertThat(bookingsPage.cardsWithout(patched.reference())).hasCount(api.liveBookingCount() - 1);

        // The original reference of the patched booking is gone from the page...
        assertThat(bookingsPage.exactTextOnPage(original.reference())).hasCount(0);

        // and every other live reference is still shown exactly once.
        List<String> otherLiveReferences = api.liveReferences().stream()
                .filter(ref -> !ref.equals(original.reference()))
                .toList();
        for (String liveRef : otherLiveReferences) {
            assertThat(bookingsPage.exactTextOnPage(liveRef)).hasCount(1);
        }

        // Check: a live card whose reference differs from the patched one.
        BookingCard anotherCard = bookingsPage.cardByReference(otherLiveReferences.getFirst());
        assertThat(anotherCard.root()).hasCount(1);
        assertThat(anotherCard.title()).not().hasText(patched.title());
        assertThat(anotherCard.reference(otherLiveReferences.getFirst())).isVisible();

        // Open View Details for the patched card; navigation must use the preserved id
        // Register the detail interception before clicking, using the id from the patched record.
        api.patchBookingDetail(patched.id());
        patchedCard.openDetails();

        assertThat(page).hasURL(Pattern.compile(".*/bookings/" + patched.id() + "$"));

        // Detail page: patched fields aligned with the list
        assertThat(detailPage.paymentSummaryHeading()).isVisible();
        assertThat(detailPage.breadcrumbReference(patched.reference())).isVisible();
        assertThat(detailPage.title()).hasText(patched.title());
        assertThat(detailPage.paymentTickets()).hasText(String.valueOf(patched.tickets()));
        assertThat(detailPage.paymentTotalPaid()).hasText(patched.displayTotal());

        /* Original, un-patched live field: the e-mail from the live list record, the live Detail API
         and the UI must all agree.
        */
        assertTrue(api.detailHitCount() >= 1, "The detail API call was not intercepted");
        assertEquals(api.liveDetailEmail(), original.email(), "Live detail e-mail must equal the live list e-mail");
        assertThat(detailPage.customerEmail()).hasText(original.email());

        // Back to My Bookings: re-find the same card by reference and compare the total
        String totalShownOnDetail = detailPage.paymentTotalPaid().innerText().trim();

        detailPage.backToMyBookings();

        assertThat(page).hasURL(Pattern.compile(".*/bookings$"));
        assertThat(bookingsPage.heading()).isVisible();

        BookingCard cardAfterReturn = bookingsPage.cardByReference(patched.reference());

        assertThat(cardAfterReturn.root()).hasCount(1);
        assertThat(cardAfterReturn.total(totalShownOnDetail)).isVisible();
        assertThat(cardAfterReturn.title()).hasText(patched.title());
    }
}
