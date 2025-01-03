package co.paydeck.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testCustomerBuilder() {
        Customer customer = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("+2348012345678")
            .countryCode("NG")
            .build();

        assertAll("Customer properties",
            () -> assertEquals("John", customer.getFirstName()),
            () -> assertEquals("Doe", customer.getLastName()),
            () -> assertEquals("john.doe@example.com", customer.getEmail()),
            () -> assertEquals("+2348012345678", customer.getPhoneNumber()),
            () -> assertEquals("NG", customer.getCountryCode())
        );
    }

    @Test
    void testCustomerEquality() {
        Customer customer1 = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        Customer customer2 = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        assertEquals(customer1, customer2, "Equal customers should be equal");
    }

    @Test
    void testHashCode() {
        Customer customer1 = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        Customer customer2 = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        assertEquals(customer1.hashCode(), customer2.hashCode(), 
            "Equal customers should have same hash code");
    }

    @Test
    void testToString() {
        Customer customer = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        String toString = customer.toString();
        
        assertAll("Customer toString",
            () -> assertTrue(toString.contains("firstName=John")),
            () -> assertTrue(toString.contains("lastName=Doe")),
            () -> assertTrue(toString.contains("email=john.doe@example.com"))
        );
    }

    @Test
    void testPartialBuild() {
        Customer customer = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        assertAll("Partial build properties",
            () -> assertEquals("John", customer.getFirstName()),
            () -> assertEquals("Doe", customer.getLastName()),
            () -> assertNull(customer.getEmail()),
            () -> assertNull(customer.getPhoneNumber()),
            () -> assertNull(customer.getCountryCode())
        );
    }

    @Test
    void testSetters() {
        Customer customer = Customer.builder().build();
        
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhoneNumber("+2348012345678");
        customer.setCountryCode("NG");

        assertAll("Customer setters",
            () -> assertEquals("John", customer.getFirstName()),
            () -> assertEquals("Doe", customer.getLastName()),
            () -> assertEquals("john.doe@example.com", customer.getEmail()),
            () -> assertEquals("+2348012345678", customer.getPhoneNumber()),
            () -> assertEquals("NG", customer.getCountryCode())
        );
    }
}
